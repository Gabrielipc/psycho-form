package com.uam.psychoform.instrument.service;

import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.instrument.model.*;
import com.uam.psychoform.instrument.repository.*;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.uam.psychoform.storage.ObjectStorageService;

@Service
@Transactional(readOnly = true)
public class ItemImageStorageService {
    private static final long MAX_IMAGE_BYTES = 25L * 1024L * 1024L;
    private final ItemLookupRepository items;
    private final RecursoMultimediaRepository recursos;
    private final ImagenItemRepository imagenes;
    private final UsuarioRepository usuarios;
    private final CurrentActor currentActor;
    private final AuditLogService audit;
    private final Clock clock;
    private final ObjectStorageService storage;

    public ItemImageStorageService(ItemLookupRepository items, RecursoMultimediaRepository recursos,
            ImagenItemRepository imagenes, UsuarioRepository usuarios, CurrentActor currentActor,
            AuditLogService audit, Clock clock, ObjectStorageService storage) {
        this.items = items;
        this.recursos = recursos;
        this.imagenes = imagenes;
        this.usuarios = usuarios;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
        this.storage = storage;
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<ImageView> listImages(long itemId) {
        return imagenes.findByItemIdOrderByNumeroOrdenAsc(itemId).stream().map(this::toView).toList();
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<ImageView> listImagesByTest(long testId) {
        return imagenes.findByTestIdOrderByInstrument(testId).stream().map(this::toView).toList();
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER)
    public List<ImageView> listImagesBySubtest(long subtestId) {
        return imagenes.findBySubtestIdOrderByInstrument(subtestId).stream().map(this::toView).toList();
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public ImageView upload(long itemId, MultipartFile file, String role, Integer order, String altText) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo requerido");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")
                && !contentType.equals("image/webp")) {
            throw new IllegalArgumentException("Solo se permiten imagenes PNG, JPG o WebP");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("La imagen excede 25MB");
        }
        Item item = items.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item no encontrado: " + itemId));
        String extension = extensionFor(contentType);
        String safeName = UUID.randomUUID() + extension;
        String objectPath = objectPath(item, safeName);
        try {
            byte[] bytes = file.getBytes();
            storage.putObject(objectPath, contentType, bytes);

            RecursoMultimedia resource = new RecursoMultimedia();
            resource.setTipoRecurso(TipoRecurso.IMAGEN);
            resource.setNombreArchivo(file.getOriginalFilename() == null ? safeName : file.getOriginalFilename());
            resource.setRutaAlmacenamiento(objectPath);
            resource.setTipoMime(contentType);
            resource.setTamanoBytes(file.getSize());
            resource.setHashIntegridad(sha256(bytes));
            resource.setEsConfidencial(true);
            resource.setRequiereAutorizacion(true);
            resource.setSubidoPor(currentUser());
            resource.setSubidoEn(LocalDateTime.now(clock));
            recursos.save(resource);

            ImagenItem image = new ImagenItem();
            image.setItem(item);
            image.setRecurso(resource);
            image.setRolImagen(role == null || role.isBlank() ? "ITEM" : role);
            image.setNumeroOrden(order == null ? imagenes.findByItemIdOrderByNumeroOrdenAsc(itemId).size() + 1 : order);
            image.setTextoAlternativo(altText);
            imagenes.save(image);
            audit.recordTrusted(new AuditLogService.AuditEvent("IMAGEN_ITEM_SUBIDA", "item", String.valueOf(itemId),
                    null, "{\"recursoId\":" + resource.getId() + "}", null, null));
            return toView(image);
        } catch (Exception ex) {
            try {
                storage.deleteObject(objectPath);
            } catch (RuntimeException ignored) {
                // Best-effort cleanup if database persistence fails after S3 upload.
            }
            if (ex instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("No se pudo leer el archivo de imagen", ex);
        }
    }

    @PreAuthorize(SecurityPermissions.TEST_LEER + " or isAuthenticated()")
    public StoredFile readResource(long resourceId) {
        RecursoMultimedia resource = recursos.findById(resourceId)
                .orElseThrow(() -> new EntityNotFoundException("Recurso no encontrado: " + resourceId));
        byte[] bytes = storage.getObject(resource.getRutaAlmacenamiento());
        return new StoredFile(resource.getNombreArchivo(), resource.getTipoMime(), bytes);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.TEST_CREAR)
    public void deleteImage(long imageId) {
        ImagenItem image = imagenes.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada: " + imageId));
        Long itemId = image.getItem().getId();
        RecursoMultimedia resource = image.getRecurso();
        storage.deleteObject(resource.getRutaAlmacenamiento());
        imagenes.delete(image);
        recursos.delete(resource);
        audit.recordTrusted(new AuditLogService.AuditEvent("IMAGEN_ITEM_ELIMINADA", "item", String.valueOf(itemId),
                "{\"imagenId\":" + imageId + "}", null, null, null));
    }

    private ImageView toView(ImagenItem image) {
        RecursoMultimedia resource = image.getRecurso();
        Item item = image.getItem();
        Subtest subtest = item.getSubtest();
        VersionTest version = subtest.getVersionTest();
        TestPsicologico test = version.getTest();
        return new ImageView(image.getId(), resource.getId(), test.getId(), version.getId(), subtest.getId(),
                item.getId(), item.getNumeroOrden(), image.getRolImagen(), image.getNumeroOrden(),
                image.getTextoAlternativo(), resource.getNombreArchivo(),
                resource.getTipoMime(), resource.getTamanoBytes(), "/items/images/resources/" + resource.getId());
    }

    private static String objectPath(Item item, String safeName) {
        Subtest subtest = item.getSubtest();
        VersionTest version = subtest.getVersionTest();
        TestPsicologico test = version.getTest();
        return "tests/" + slug(test.getCodigoTest())
                + "/versions/" + slug(version.getNumeroVersion())
                + "/subtests/" + slug(subtest.getCodigoSubtest())
                + "/items/" + item.getNumeroOrden()
                + "/" + safeName;
    }

    private static String slug(String value) {
        if (value == null || value.isBlank()) {
            return "sin-codigo";
        }
        return value.toLowerCase()
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }

    private Usuario currentUser() {
        return usuarios.findById(currentActor.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    public record ImageView(Long id, Long resourceId, Long testId, Long versionId, Long subtestId, Long itemId,
            Integer itemOrder, String role, Integer order, String altText, String fileName, String mimeType,
            Long sizeBytes, String url) {
    }

    public record StoredFile(String fileName, String mimeType, byte[] bytes) {
    }
}
