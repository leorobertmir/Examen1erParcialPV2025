package com.simpsons;

import com.simpsons.model.Personajes;
import com.simpsons.model.SimpsonResponse;
import com.simpsons.service.SimpsonService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

public class PrimaryController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button randomButton;
    @FXML private Button loadMoreButton;
    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane charactersContainer;
    
    private SimpsonService simpsonService;
    private List<Personajes> currentCharactersList;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    public void initialize() {
        // TODO: Inicializar simpsonService con new SimpsonService()
        // TODO: Inicializar currentCharactersList como new ArrayList<>()
        // TODO: Llamar a setupFlowPaneLayout()
        // TODO: Llamar a loadInitialCharacters()
    }
    
    private void setupFlowPaneLayout() {
        // Configurar FlowPane para que ajuste automáticamente
        charactersContainer.prefWrapLengthProperty().bind(
            scrollPane.widthProperty().subtract(60) // padding izquierdo + derecho
        );
        
        // Listener para ajustar el ancho de las tarjetas cuando cambie el tamaño
        scrollPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            updateCardWidths();
        });
        
        // Ajustar también cuando se agreguen nuevas tarjetas
        charactersContainer.getChildren().addListener((javafx.collections.ListChangeListener.Change<? extends javafx.scene.Node> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    updateCardWidths();
                }
            }
        });
    }
    
    private void updateCardWidths() {
        double containerWidth = scrollPane.getWidth();
        if (containerWidth > 0) {
            // Ancho disponible = ancho del scrollPane - padding (30px a cada lado)
            double availableWidth = containerWidth - 60;
            // Ancho de cada tarjeta = (ancho disponible - 3 gaps de 20px) / 4 columnas
            double cardWidth = (availableWidth - 60) / 4;
            
            // Asegurar un ancho mínimo y máximo razonable
            cardWidth = Math.max(250, Math.min(cardWidth, 320));
            
            // Actualizar ancho de todas las tarjetas existentes
            for (var node : charactersContainer.getChildren()) {
                if (node instanceof VBox) {
                    VBox card = (VBox) node;
                    card.setPrefWidth(cardWidth);
                    card.setMaxWidth(cardWidth);
                    card.setMinWidth(cardWidth);
                }
            }
        }
    }

    @FXML
    private void searchCharacter() {
        // TODO: Obtener texto del campo de búsqueda y validar que no esté vacío
        // TODO: Limpiar contenedor y mostrar indicador de carga
        // TODO: Buscar personaje en currentCharactersList usando stream
        // TODO: Si se encuentra, ocultar indicador, limpiar y mostrar personaje
        // TODO: Si no se encuentra, ocultar indicador y mostrar alerta informativa
    }

    @FXML
    private void getRandomCharacter() {
        // TODO: Limpiar contenedor y mostrar indicador de carga
        // TODO: Crear Task<Personajes> que genere ID aleatorio y obtenga personaje
        // TODO: Configurar setOnSucceeded para mostrar personaje
        // TODO: Configurar setOnFailed para mostrar error
        // TODO: Iniciar Task en nuevo hilo
    }

    @FXML
    private void loadMoreCharacters() {
        // TODO: Validar isLoading y hasMorePages
        // TODO: Establecer isLoading = true y mostrar indicador
        // TODO: Crear Task<SimpsonResponse> para obtener personajes
        // TODO: Configurar setOnSucceeded para procesar respuesta y actualizar UI
        // TODO: Configurar setOnFailed para manejar errores
        // TODO: Iniciar Task en nuevo hilo
    }

    private void loadInitialCharacters() {
        loadMoreCharacters();
    }

    private void displayCharacter(Personajes character) {
        Platform.runLater(() -> {
            VBox characterCard = createCharacterCard(character);
            charactersContainer.getChildren().add(characterCard);
            // Ajustar ancho después de agregar
            updateCardWidths();
        });
    }

    private VBox createCharacterCard(Personajes character) {
        VBox card = new VBox();
        card.getStyleClass().add("character-card");
        
        // Calcular ancho dinámico para mostrar 4 columnas
        double containerWidth = scrollPane.getWidth() > 0 ? scrollPane.getWidth() : 1200;
        double availableWidth = containerWidth - 60; // padding izquierdo + derecho
        double cardWidth = (availableWidth - 60) / 4; // 4 columnas con 3 gaps de 20px = 60px
        
        // Asegurar un ancho mínimo y máximo razonable
        cardWidth = Math.max(250, Math.min(cardWidth, 320));
        
        card.setPrefWidth(cardWidth);
        card.setMaxWidth(cardWidth);
        card.setMinWidth(cardWidth);
        
        // Contenedor de imagen con fondo degradado
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("character-image-container");
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(220);
        
        ImageView imageView = createCharacterImage(character);
        imageContainer.getChildren().add(imageView);
        
        // TODO: Crear contentContainer (VBox con espaciado 12 y clase "character-content")
        // TODO: Crear label del nombre con clase "character-name"
        // TODO: Crear label de ocupación con emoji 💼 y clase "character-occupation"
        // TODO: Crear infoRow (HBox) con edad y estado
        // TODO: Crear label de frase con clase "character-phrase"
        // TODO: Crear botón de detalles con evento que llame a showCharacterDetails
        // TODO: Agregar todos los componentes al contentContainer
        // TODO: Agregar imageContainer y contentContainer a la tarjeta
        
        return card;
    }

    // MÉTODO COMPLETO - NO MODIFICAR
    private ImageView createCharacterImage(Personajes character) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        
        if (character.getPortraitPath() != null && !character.getPortraitPath().isEmpty()) {
            // El portrait_path viene como "/character/X.webp" desde la API
            String portraitPathTemp = character.getPortraitPath().trim();
            
            // Asegurar que empiece con "/"
            if (!portraitPathTemp.startsWith("/")) {
                portraitPathTemp = "/" + portraitPathTemp;
            }
            
            // Crear variable final para usar en lambdas
            final String portraitPath = portraitPathTemp;
            final String imageUrl = "https://cdn.thesimpsonsapi.com/200" + portraitPath;
            
            System.out.println("📸 Cargando imagen para: " + character.getName());
            System.out.println("   URL: " + imageUrl);
            System.out.println("   Portrait path original: " + character.getPortraitPath());
            
            // PRIMERO: Intentar carga directa con JavaFX Image (más simple)
            Image directImage = new Image(imageUrl, true);
            imageView.setImage(directImage);
            
            // Monitorear si la carga directa funciona
            directImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0) {
                    if (!directImage.isError()) {
                        System.out.println("✅ Carga directa exitosa para: " + character.getName());
                    } else {
                        System.out.println("⚠️ Carga directa falló, intentando descarga manual para: " + character.getName());
                        downloadAndLoadImage(imageView, imageUrl, portraitPath, character.getName());
                    }
                }
            });
            
            directImage.errorProperty().addListener((obs, wasError, isNowError) -> {
                if (isNowError) {
                    System.out.println("⚠️ Error en carga directa, intentando descarga manual para: " + character.getName());
                    downloadAndLoadImage(imageView, imageUrl, portraitPath, character.getName());
                }
            });
        } else {
            System.out.println("⚠️ No hay portrait_path para: " + character.getName());
        }
        
        return imageView;
    }
    
    // MÉTODO COMPLETO - NO MODIFICAR
    private void downloadAndLoadImage(ImageView imageView, String primaryUrl, String portraitPath, String characterName) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        
        // URLs a intentar en orden
        final String[] urls = {
            primaryUrl,
            "https://cdn.thesimpsonsapi.com/300" + portraitPath,
            "https://cdn.thesimpsonsapi.com" + portraitPath
        };
        
        downloadImageBytes(client, imageView, urls, 0, characterName);
    }
    
    // MÉTODO COMPLETO - NO MODIFICAR
    private void downloadImageBytes(HttpClient client, ImageView imageView, String[] urls, int index, String characterName) {
        if (index >= urls.length) {
            System.err.println("❌ No se pudo cargar imagen para: " + characterName + " después de " + urls.length + " intentos");
            Platform.runLater(() -> {
                // Mostrar placeholder visual
                imageView.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 90;");
            });
            return;
        }
        
        final String url = urls[index];
        System.out.println("🔄 Intento " + (index + 1) + " para " + characterName + ": " + url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Referer", "https://thesimpsonsapi.com/")
                    .header("Origin", "https://thesimpsonsapi.com")
                    .header("Cache-Control", "no-cache")
                    .build();
            
            client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    System.out.println("📥 Respuesta HTTP " + response.statusCode() + " para " + characterName);
                    
                    if (response.statusCode() == 200) {
                        byte[] imageBytes = response.body();
                        System.out.println("📦 Bytes descargados: " + (imageBytes != null ? imageBytes.length : 0) + " para " + characterName);
                        
                        if (imageBytes != null && imageBytes.length > 0) {
                            // Verificar que es una imagen válida (WebP empieza con RIFF)
                            if (imageBytes.length >= 4) {
                                String header = new String(imageBytes, 0, Math.min(4, imageBytes.length));
                                System.out.println("🔍 Header de imagen: " + bytesToHex(imageBytes, 0, 4) + " para " + characterName);
                            }
                            
                            Platform.runLater(() -> {
                                try {
                                    // Leer imagen WebP usando ImageIO con soporte de TwelveMonkeys
                                    ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                                    BufferedImage bufferedImage = ImageIO.read(bais);
                                    
                                    if (bufferedImage != null) {
                                        // Convertir BufferedImage a JavaFX Image
                                        Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                                        
                                        // Establecer imagen en ImageView
                                        imageView.setImage(fxImage);
                                        System.out.println("✅ Imagen WebP cargada exitosamente para: " + characterName);
                                    } else {
                                        System.err.println("❌ No se pudo leer imagen WebP para: " + characterName);
                                        if (index + 1 < urls.length) {
                                            downloadImageBytes(client, imageView, urls, index + 1, characterName);
                                        }
                                    }
                                    
                                } catch (Exception e) {
                                    System.err.println("❌ Excepción procesando imagen WebP para " + characterName + ": " + e.getMessage());
                                    e.printStackTrace();
                                    if (index + 1 < urls.length) {
                                        downloadImageBytes(client, imageView, urls, index + 1, characterName);
                                    }
                                }
                            });
                        } else {
                            System.err.println("❌ Imagen vacía o nula desde: " + url);
                            downloadImageBytes(client, imageView, urls, index + 1, characterName);
                        }
                    } else {
                        System.err.println("❌ HTTP " + response.statusCode() + " desde: " + url);
                        downloadImageBytes(client, imageView, urls, index + 1, characterName);
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Excepción descargando " + url + " para " + characterName + ": " + throwable.getMessage());
                    throwable.printStackTrace();
                    downloadImageBytes(client, imageView, urls, index + 1, characterName);
                    return null;
                });
        } catch (Exception e) {
            System.err.println("❌ Error creando request para " + characterName + ": " + e.getMessage());
            e.printStackTrace();
            downloadImageBytes(client, imageView, urls, index + 1, characterName);
        }
    }
    
    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < Math.min(offset + length, bytes.length); i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }


    private void showCharacterDetails(Personajes character) {
        // TODO: Crear Alert de tipo INFORMATION
        // TODO: Establecer título y header
        // TODO: Crear StringBuilder y agregar todos los detalles
        // TODO: Agregar frases famosas si existen
        // TODO: Configurar Alert y mostrarlo
    }

    private void clearCharactersContainer() {
        charactersContainer.getChildren().clear();
    }

    private void showLoadingIndicator() {
        Label loadingLabel = new Label("Cargando personajes...");
        loadingLabel.getStyleClass().add("loading-indicator");
        loadingLabel.setAlignment(Pos.CENTER);
        charactersContainer.getChildren().add(loadingLabel);
    }

    private void hideLoadingIndicator() {
        charactersContainer.getChildren().removeIf(node -> 
            node instanceof Label && ((Label) node).getText().contains("Cargando"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

