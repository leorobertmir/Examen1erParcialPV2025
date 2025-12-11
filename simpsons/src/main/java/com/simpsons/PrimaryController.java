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

    
    // TAREA 1: INITIALIZE 
    
    public void initialize() {
        // Inicializar servicio de Simpsons
        simpsonService = new SimpsonService();
        
        // Inicializar lista de personajes
        currentCharactersList = new ArrayList<>();
        
        // Configurar el layout del FlowPane
        setupFlowPaneLayout();
        
        // Cargar personajes iniciales
        loadInitialCharacters();
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

    
    // TAREA 2: SEARCH CHARACTER 
    

    @FXML
    private void searchCharacter() {
        // Obtener texto del campo de búsqueda
        String searchText = searchField.getText().trim();
        
        // Validar que no esté vacío
        if (searchText.isEmpty()) {
            showAlert("Error", "Por favor ingresa el nombre de un personaje");
            return;
        }
        
        // Limpiar contenedor y mostrar indicador de carga
        clearCharactersContainer();
        showLoadingIndicator();
        
        // Buscar personaje en currentCharactersList usando stream
        Personajes foundCharacter = currentCharactersList.stream()
                .filter(character -> character.getName() != null && 
                        character.getName().toLowerCase().contains(searchText.toLowerCase()))
                .findFirst()
                .orElse(null);
        
        // Procesar resultado de búsqueda
        if (foundCharacter != null) {
            // Si se encuentra
            hideLoadingIndicator();
            clearCharactersContainer();
            displayCharacter(foundCharacter);
        } else {
            // Si no se encuentra
            hideLoadingIndicator();
            showAlert("Información", 
                    "Personaje no encontrado en la lista actual. Intenta cargar más personajes o busca por ID.");
        }
    }

    
    // TAREA 3: RANDOM CHARACTER 
    

    @FXML
    private void getRandomCharacter() {
        // Limpiar contenedor y mostrar indicador de carga
        clearCharactersContainer();
        showLoadingIndicator();
        
        // Crear Task para obtener personaje aleatorio
        Task<Personajes> randomTask = new Task<Personajes>() {
            @Override
            protected Personajes call() throws Exception {
                // Generar ID aleatorio entre 1 y 1182
                Random random = new Random();
                int randomId = random.nextInt(1182) + 1;
                
                // Obtener personaje de la API
                return simpsonService.getCharacterById(randomId).get();
            }
        };
        
        // Configurar acción al completar exitosamente
        randomTask.setOnSucceeded(event -> {
            hideLoadingIndicator();
            
            Personajes character = randomTask.getValue();
            
            if (character != null) {
                // Limpiar lista actual y agregar personaje aleatorio
                currentCharactersList.clear();
                currentCharactersList.add(character);
                
                // Mostrar personaje
                displayCharacter(character);
            }
        });
        
        // Configurar acción al fallar
        randomTask.setOnFailed(event -> {
            hideLoadingIndicator();
            showAlert("Error", "No se pudo obtener un personaje aleatorio");
        });
        
        // Iniciar Task en nuevo hilo
        new Thread(randomTask).start();
    }

    
    // TAREA 4: LOAD MORE CHARACTERS 
    

    @FXML
    private void loadMoreCharacters() {
        // Validar que no se esté cargando y que haya más páginas
        if (isLoading || !hasMorePages) {
            return;
        }
        
        // Establecer bandera de carga
        isLoading = true;
        
        // Mostrar indicador de carga
        showLoadingIndicator();
        
        // Crear Task para obtener personajes de la API
        Task<SimpsonResponse> loadTask = new Task<SimpsonResponse>() {
            @Override
            protected SimpsonResponse call() throws Exception {
                // Obtener personajes de la página actual
                return simpsonService.getCharacters(currentPage).get();
            }
        };
        
        // Configurar acción al completar exitosamente
        loadTask.setOnSucceeded(event -> {
            hideLoadingIndicator();
            
            SimpsonResponse response = loadTask.getValue();
            
            if (response != null && response.getResults() != null) {
                // Obtener lista de nuevos personajes
                List<Personajes> newCharacters = response.getResults();
                
                // Si es la primera página, limpiar lista actual
                if (currentPage == 1) {
                    currentCharactersList.clear();
                }
                
                // Agregar nuevos personajes a la lista
                currentCharactersList.addAll(newCharacters);
                
                // Mostrar cada personaje en la UI
                for (Personajes character : newCharacters) {
                    displayCharacter(character);
                }
                
                // Verificar si hay más páginas
                hasMorePages = response.getNext() != null && !response.getNext().isEmpty();
                
                // Incrementar página para próxima carga
                currentPage++;
                
                // Si no hay más páginas, deshabilitar botón
                if (!hasMorePages) {
                    loadMoreButton.setDisable(true);
                    loadMoreButton.setText("No hay más personajes");
                }
            }
            
            // Restablecer bandera de carga
            isLoading = false;
        });
        
        // Configurar acción al fallar
        loadTask.setOnFailed(event -> {
            hideLoadingIndicator();
            showAlert("Error", "No se pudieron cargar más personajes");
            isLoading = false;
        });
        
        // Iniciar Task en nuevo hilo
        new Thread(loadTask).start();
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

    
    // TAREA 5: CREATE CHARACTER CARD 
    

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
        
        // ===== COMPONENTES DE LA TARJETA =====
        
        // Crear contenedor de contenido
        VBox contentContainer = new VBox(12);
        contentContainer.getStyleClass().add("character-content");

        // Label del nombre
        Label nameLabel = new Label(character.getName());
        nameLabel.getStyleClass().add("character-name");

        // Label de ocupación
        String occupation = (character.getOccupation() != null && !character.getOccupation().isEmpty()) 
                ? character.getOccupation() 
                : "Sin ocupación";
        Label occupationLabel = new Label("💼 " + occupation);
        occupationLabel.getStyleClass().add("character-occupation");

        // Fila de información (edad y estado)
        HBox infoRow = new HBox(10);
        infoRow.getStyleClass().add("character-info-row");
        infoRow.setAlignment(Pos.CENTER_LEFT);

        // Edad (solo si existe y es mayor a 0)
        if (character.getAge() != null && character.getAge() > 0) {
            Label ageLabel = new Label("🎂 " + character.getAge() + " años");
            ageLabel.getStyleClass().add("character-age");
            infoRow.getChildren().add(ageLabel);
        }

        // Estado
        String status = character.getStatus() != null ? character.getStatus() : "Desconocido";
        Label statusLabel = new Label(status);

        if (status.equalsIgnoreCase("Alive")) {
            statusLabel.getStyleClass().add("character-status-alive");
        } else if (status.equalsIgnoreCase("Deceased")) {
            statusLabel.getStyleClass().add("character-status-deceased");
        } else {
            statusLabel.getStyleClass().add("character-age");
        }

        infoRow.getChildren().add(statusLabel);

        // Label de frase famosa
        Label phraseLabel = new Label();

        if (character.getPhrases() != null && character.getPhrases().length > 0) {
            phraseLabel.setText("\"" + character.getPhrases()[0] + "\"");
        } else {
            phraseLabel.setText("Sin frase famosa");
        }

        phraseLabel.getStyleClass().add("character-phrase");

        // Botón de detalles
        Button detailsButton = new Button("Ver Detalles");
        detailsButton.getStyleClass().add("character-details-button");
        detailsButton.setOnAction(e -> showCharacterDetails(character));

        // Agregar todos los componentes al contenedor de contenido
        contentContainer.getChildren().addAll(
                nameLabel,
                occupationLabel,
                infoRow,
                phraseLabel,
                detailsButton
        );

        // Agregar imageContainer y contentContainer a la tarjeta
        card.getChildren().addAll(imageContainer, contentContainer);
        
        return card;
    }

    
    // MÉTODOS DE CARGA DE IMAGEN (NO MODIFICAR)
    

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

    
    // TAREA 6: SHOW CHARACTER DETAILS 
    

    private void showCharacterDetails(Personajes character) {
        // Crear Alert de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        
        // Establecer título
        alert.setTitle("Detalles de " + character.getName());
        
        // Establecer header como null (sin header)
        alert.setHeaderText(null);
        
        // Crear StringBuilder para construir detalles
        StringBuilder details = new StringBuilder();
        
        // Agregar información básica
        details.append("ID: ").append(character.getId()).append("\n");
        details.append("Nombre: ").append(character.getName()).append("\n");
        
        // Agregar edad si existe
        if (character.getAge() != null) {
            details.append("Edad: ").append(character.getAge()).append(" años\n");
        }
        
        // Agregar fecha de nacimiento si existe
        if (character.getBirthdate() != null && !character.getBirthdate().isEmpty()) {
            details.append("Fecha de Nacimiento: ").append(character.getBirthdate()).append("\n");
        }
        
        // Agregar género si existe
        if (character.getGender() != null && !character.getGender().isEmpty()) {
            details.append("Género: ").append(character.getGender()).append("\n");
        }
        
        // Agregar ocupación si existe
        if (character.getOccupation() != null && !character.getOccupation().isEmpty()) {
            details.append("Ocupación: ").append(character.getOccupation()).append("\n");
        }
        
        // Agregar estado
        String status = character.getStatus() != null ? character.getStatus() : "Desconocido";
        details.append("Estado: ").append(status).append("\n");
        
        // Agregar frases famosas si existen
        if (character.getPhrases() != null && character.getPhrases().length > 0) {
            details.append("\nFrases Famosas:\n");
            
            for (String phrase : character.getPhrases()) {
                details.append("• \"").append(phrase).append("\"\n");
            }
        }
        
        // Establecer contenido del Alert
        alert.setContentText(details.toString());
        
        // Hacer el Alert redimensionable
        alert.setResizable(true);
        
        // Establecer ancho preferido
        alert.getDialogPane().setPrefWidth(500);
        
        // Mostrar Alert y esperar
        alert.showAndWait();
    }

    
    // MÉTODOS AUXILIARES
    
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