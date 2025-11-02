package com.notia;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import shared.Assistant;

import java.util.List;
import java.util.Optional;

public class App extends Application {

    private enum ViewState {
        PREVIEW,
        EDIT,
        SPLIT
    }

    private TextArea markdownEditor;
    private WebView htmlViewer;
    private ListView<Note> noteList;
    private ListView<Category> categoryList;
    private ListView<Tag> tagList;
    private ObservableList<Note> notes;
    private ObservableList<Category> categories;
    private ObservableList<Tag> tags;
    private Note currentNote;
    private BorderPane centerPane;
    private SplitPane splitPane;
    private TextField searchField;
    private SplitPane mainSplitPane;
    private VBox sideBar;
    private boolean sideBarVisible = true;
    private VBox chatSidebar;
    private ScrollPane chatScrollPane;
    private VBox chatMessagesContainer;
    private TextField chatInputField;
    private Button chatSendButton;
    private boolean chatSidebarVisible = false;
    private Assistant chatAssistant;
    private ProgressIndicator chatLoadingIndicator;

    private ViewState currentView = ViewState.PREVIEW;

    private Parser parser = Parser.builder().build();
    private HtmlRenderer renderer = HtmlRenderer.builder().build();

    public static void main(String[] args) {
        Database.initialize();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        markdownEditor = new TextArea();
        // Enable emoji support in TextArea with a font that supports emoji
        markdownEditor.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; -fx-font-size: 14px;");
        
        htmlViewer = new WebView();
        noteList = new ListView<>();
        categoryList = new ListView<>();
        tagList = new ListView<>();
        centerPane = new BorderPane();
        splitPane = new SplitPane(markdownEditor, htmlViewer);
        searchField = new TextField();

        notes = FXCollections.observableArrayList(Database.getAllNotes());
        noteList.setItems(notes);

        categories = FXCollections.observableArrayList(Database.getAllCategories());
        categoryList.setItems(categories);

        tags = FXCollections.observableArrayList(Database.getAllTags());
        tagList.setItems(tags);

        // Initialize chat assistant in background
        initializeChatAssistant();

        noteList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadNoteContent(newValue);
            }
        });

        categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                notes.setAll(Database.getNotesByCategory(newValue.getId()));
            }
        });

        tagList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                notes.setAll(Database.getNotesByTag(newValue.getId()));
            }
        });

        markdownEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            updateHtmlView(newValue);
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchNotes(newValue);
        });

        ToolBar toolBar = createToolBar();

        // Create Rosé Pine sidebar with category/tag management
        Label categoriesLabel = new Label("Categories");
        categoriesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8px; -fx-text-fill: #ebbcba;");
        
        // Category buttons
        HBox categoryButtons = new HBox(5);
        Button addCategoryButton = new Button("➕ Add");
        addCategoryButton.setOnAction(e -> addCategoryToCurrentNote());
        addCategoryButton.setStyle("-fx-font-size: 10px; -fx-padding: 4px 8px;");
        
        Button removeCategoryButton = new Button("➖ Remove");
        removeCategoryButton.setOnAction(e -> removeCategoryFromCurrentNote());
        removeCategoryButton.setStyle("-fx-font-size: 10px; -fx-padding: 4px 8px;");
        
        categoryButtons.getChildren().addAll(addCategoryButton, removeCategoryButton);
        categoryButtons.setPadding(new Insets(0, 8, 8, 8));
        
        Label tagsLabel = new Label("Tags");
        tagsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8px; -fx-text-fill: #ebbcba;");
        
        // Tag buttons
        HBox tagButtons = new HBox(5);
        Button addTagButton = new Button("➕ Add");
        addTagButton.setOnAction(e -> addTagToCurrentNote());
        addTagButton.setStyle("-fx-font-size: 10px; -fx-padding: 4px 8px;");
        
        Button removeTagButton = new Button("➖ Remove");
        removeTagButton.setOnAction(e -> removeTagFromCurrentNote());
        removeTagButton.setStyle("-fx-font-size: 10px; -fx-padding: 4px 8px;");
        
        tagButtons.getChildren().addAll(addTagButton, removeTagButton);
        tagButtons.setPadding(new Insets(0, 8, 8, 8));
        
        sideBar = new VBox(10, 
            categoriesLabel, 
            categoryList, 
            categoryButtons,
            tagsLabel, 
            tagList,
            tagButtons);
        sideBar.setPadding(new Insets(10));
        sideBar.setStyle("-fx-background-color: #191724;");

        chatSidebar = createChatSidebar();

        mainSplitPane = new SplitPane(noteList, centerPane, sideBar);
        mainSplitPane.setDividerPositions(0.2, 0.8);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(mainSplitPane);

        Scene scene = new Scene(root, 1280, 768);
        
        // Load Material Design theme
        try {
            String materialCss = getClass().getResource("/material-theme.css").toExternalForm();
            scene.getStylesheets().add(materialCss);
        } catch (Exception e) {
            System.err.println("Could not load Material theme CSS: " + e.getMessage());
        }
        
        // Load chat styles
        try {
            String chatCss = getClass().getResource("/chat-styles.css").toExternalForm();
            scene.getStylesheets().add(chatCss);
        } catch (Exception e) {
            System.err.println("Could not load chat CSS: " + e.getMessage());
        }
        
        stage.setTitle("Notia - Markdown Note Taking");
        stage.setScene(scene);
        stage.show();

        updateView();
    }

    private ToolBar createToolBar() {
        Button newNoteButton = new Button("✚ New");
        newNoteButton.setOnAction(e -> createNewNote());

        Button saveNoteButton = new Button("💾 Save");
        saveNoteButton.setOnAction(e -> saveCurrentNote());

        Button deleteNoteButton = new Button("🗑 Delete");
        deleteNoteButton.setOnAction(e -> deleteCurrentNote());

        Button previewButton = new Button("👁 Preview");
        previewButton.setOnAction(e -> {
            currentView = ViewState.PREVIEW;
            updateView();
        });

        Button editButton = new Button("✏ Edit");
        editButton.setOnAction(e -> {
            currentView = ViewState.EDIT;
            updateView();
        });

        Button splitViewButton = new Button("⬌ Split");
        splitViewButton.setOnAction(e -> {
            currentView = ViewState.SPLIT;
            updateView();
        });

        Button toggleSidebarButton = new Button("☰ Sidebar");
        toggleSidebarButton.setOnAction(e -> toggleSidebar());

        Button toggleChatButton = new Button("💬 AI Chat");
        toggleChatButton.setOnAction(e -> toggleChatSidebar());

        return new ToolBar(newNoteButton, saveNoteButton, deleteNoteButton, new Separator(),
                searchField, new Separator(),
                previewButton, editButton, splitViewButton, new Separator(),
                toggleSidebarButton, toggleChatButton);
    }

    private void createNewNote() {
        currentNote = null;
        markdownEditor.clear();
        noteList.getSelectionModel().clearSelection();
        notes.setAll(Database.getAllNotes());
        currentView = ViewState.EDIT;
        updateView();
    }

    private void saveCurrentNote() {
        String content = markdownEditor.getText();
        if (content.isEmpty()) {
            return;
        }

        String title = content.lines().findFirst().orElse("Untitled");

        if (currentNote == null) {
            // Creating a new note
            currentNote = new Note(0, title, content, null, null, false, false, 0);
        } else {
            // Updating existing note
            currentNote.setTitle(title);
            currentNote.setContent(content);
        }

        int noteId = Database.saveNote(currentNote);
        if (noteId != 0) {
            currentNote = Database.getNoteById(noteId);
            refreshNoteList();
            // Update the preview with the new content
            updateHtmlView(content);
            // Switch to preview mode to see the saved changes
            currentView = ViewState.PREVIEW;
            updateView();
        }
    }

    private void deleteCurrentNote() {
        if (currentNote != null) {
            Database.deleteNote(currentNote.getId());
            refreshNoteList();
            createNewNote();
        }
    }

    private void loadNoteContent(Note note) {
        currentNote = Database.getNoteById(note.getId());
        if (currentNote != null) {
            markdownEditor.setText(currentNote.getContent());
            currentView = ViewState.PREVIEW;
            updateView();
        }
    }

    private void refreshNoteList() {
        notes.setAll(Database.getAllNotes());
        noteList.getSelectionModel().select(currentNote);
    }

    private void updateHtmlView(String markdown) {
        Node document = parser.parse(markdown);
        String html = renderer.render(document);
        
        // Wrap HTML with Rosé Pine theme and proper emoji support
        String wrappedHtml = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "<style>" +
                "* { " +
                "  font-family: 'Segoe UI', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Android Emoji', 'EmojiSymbols', sans-serif; " +
                "}" +
                "body { " +
                "  font-family: 'Segoe UI', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Android Emoji', 'EmojiSymbols', sans-serif; " +
                "  font-size: 14px; " +
                "  line-height: 1.6; " +
                "  padding: 20px; " +
                "  background-color: #191724; " +
                "  color: #e0def4; " +
                "}" +
                "h1, h2, h3, h4, h5, h6 { " +
                "  color: #ebbcba; " +
                "  margin-top: 24px; " +
                "  margin-bottom: 16px; " +
                "  font-family: 'Segoe UI', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; " +
                "}" +
                "a { " +
                "  color: #9ccfd8; " +
                "  text-decoration: none; " +
                "}" +
                "a:hover { " +
                "  text-decoration: underline; " +
                "}" +
                "code { " +
                "  background-color: #26233a; " +
                "  color: #c4a7e7; " +
                "  padding: 2px 6px; " +
                "  border-radius: 4px; " +
                "  font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                "}" +
                "pre { " +
                "  background-color: #26233a; " +
                "  padding: 16px; " +
                "  border-radius: 8px; " +
                "  overflow-x: auto; " +
                "  border-left: 4px solid #31748f; " +
                "}" +
                "pre code { " +
                "  background-color: transparent; " +
                "  padding: 0; " +
                "}" +
                "blockquote { " +
                "  border-left: 4px solid #ebbcba; " +
                "  padding-left: 16px; " +
                "  color: #908caa; " +
                "  margin: 16px 0; " +
                "}" +
                "table { " +
                "  border-collapse: collapse; " +
                "  width: 100%; " +
                "  margin: 16px 0; " +
                "}" +
                "th, td { " +
                "  border: 1px solid #26233a; " +
                "  padding: 8px 12px; " +
                "  text-align: left; " +
                "}" +
                "th { " +
                "  background-color: #26233a; " +
                "  color: #ebbcba; " +
                "  font-weight: bold; " +
                "}" +
                "hr { " +
                "  border: none; " +
                "  border-top: 2px solid #26233a; " +
                "  margin: 24px 0; " +
                "}" +
                "p, li, td, th { " +
                "  font-family: 'Segoe UI', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; " +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" + html + "</body>" +
                "</html>";
        
        htmlViewer.getEngine().loadContent(wrappedHtml, "text/html");
    }

    private void updateView() {
        switch (currentView) {
            case PREVIEW:
                centerPane.setCenter(htmlViewer);
                break;
            case EDIT:
                centerPane.setCenter(markdownEditor);
                break;
            case SPLIT:
                splitPane.getItems().setAll(markdownEditor, htmlViewer);
                centerPane.setCenter(splitPane);
                break;
        }
    }

    private void addCategoryToCurrentNote() {
        if (currentNote == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter a category to add");
        dialog.setContentText("Category:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(categoryName -> {
            Category category = Database.getCategoryByName(categoryName);
            if (category == null) {
                int categoryId = Database.addCategory(categoryName);
                if (categoryId != 0) {
                    Database.addCategoryToNote(currentNote.getId(), categoryId);
                    categories.setAll(Database.getAllCategories());
                }
            } else {
                Database.addCategoryToNote(currentNote.getId(), category.getId());
            }
        });
    }

    private void removeCategoryFromCurrentNote() {
        if (currentNote == null) {
            return;
        }

        Category selectedCategory = categoryList.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            Database.removeCategoryFromNote(currentNote.getId(), selectedCategory.getId());
            categories.setAll(Database.getAllCategories());
        }
    }

    private void addTagToCurrentNote() {
        if (currentNote == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Tag");
        dialog.setHeaderText("Enter a tag to add");
        dialog.setContentText("Tag:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(tagName -> {
            Tag tag = Database.getTagByName(tagName);
            if (tag == null) {
                int tagId = Database.addTag(tagName);
                if (tagId != 0) {
                    Database.addTagToNote(currentNote.getId(), tagId);
                    tags.setAll(Database.getAllTags());
                }
            } else {
                Database.addTagToNote(currentNote.getId(), tag.getId());
            }
        });
    }

    private void removeTagFromCurrentNote() {
        if (currentNote == null) {
            return;
        }

        Tag selectedTag = tagList.getSelectionModel().getSelectedItem();
        if (selectedTag != null) {
            Database.removeTagFromNote(currentNote.getId(), selectedTag.getId());
            tags.setAll(Database.getAllTags());
        }
    }

    private void searchNotes(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            notes.setAll(Database.getAllNotes());
        } else {
            notes.setAll(Database.searchNotes(searchText));
        }
    }

    private void toggleSidebar() {
        sideBarVisible = !sideBarVisible;
        if (sideBarVisible) {
            if (!mainSplitPane.getItems().contains(sideBar)) {
                mainSplitPane.getItems().add(sideBar);
                mainSplitPane.setDividerPositions(0.2, 0.8);
            }
        } else {
            mainSplitPane.getItems().remove(sideBar);
            mainSplitPane.setDividerPositions(0.2);
        }
    }

    private VBox createChatSidebar() {
        VBox chatContainer = new VBox(10);
        chatContainer.setPadding(new Insets(10));
        chatContainer.setPrefWidth(350);
        chatContainer.getStyleClass().add("chat-container");

        Label chatTitle = new Label("AI Assistant");
        chatTitle.getStyleClass().add("chat-title");

        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setPadding(new Insets(10));

        chatScrollPane = new ScrollPane(chatMessagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.getStyleClass().add("chat-scroll-pane");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        chatLoadingIndicator = new ProgressIndicator();
        chatLoadingIndicator.setMaxSize(30, 30);
        chatLoadingIndicator.setVisible(false);

        chatInputField = new TextField();
        chatInputField.setPromptText("Ask about your notes...");
        chatInputField.setOnAction(e -> sendChatMessage());
        chatInputField.getStyleClass().add("chat-input");
        chatInputField.setStyle(chatInputField.getStyle() + 
            "-fx-font-family: 'Segoe UI', 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");

        chatSendButton = new Button("Send");
        chatSendButton.setOnAction(e -> sendChatMessage());
        chatSendButton.getStyleClass().add("chat-send-button");

        HBox inputBox = new HBox(10, chatInputField, chatSendButton, chatLoadingIndicator);
        HBox.setHgrow(chatInputField, Priority.ALWAYS);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        Button clearChatButton = new Button("Clear Chat");
        clearChatButton.setOnAction(e -> clearChat());
        clearChatButton.getStyleClass().add("chat-clear-button");

        chatContainer.getChildren().addAll(chatTitle, chatScrollPane, inputBox, clearChatButton);

        return chatContainer;
    }

    private void initializeChatAssistant() {
        new Thread(() -> {
            try {
                chatAssistant = ChatAssistantFactory.createAssistant();
                Platform.runLater(() -> {
                    addChatMessage("AI", "Hello! I'm your AI assistant. Ask me anything about your notes!", false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addChatMessage("System", "Error: Could not connect to AI service. Make sure ChromaDB is running and GEMINI_API_KEY is set.", false);
                });
            }
        }).start();
    }

    private void sendChatMessage() {
        String userMessage = chatInputField.getText().trim();
        if (userMessage.isEmpty() || chatAssistant == null) {
            return;
        }

        addChatMessage("You", userMessage, false);
        chatInputField.clear();
        chatInputField.setDisable(true);
        chatSendButton.setDisable(true);
        chatLoadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                String response = chatAssistant.chat(userMessage);
                
                // Validate response
                if (response == null || response.trim().isEmpty()) {
                    response = "I received your message but couldn't generate a response. Please try again.";
                }
                
                final String finalResponse = response;
                Platform.runLater(() -> {
                    addChatMessage("AI", finalResponse, false);
                    chatInputField.setDisable(false);
                    chatSendButton.setDisable(false);
                    chatLoadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String errorMsg = "Error: " + e.getMessage();
                    if (e.getMessage() != null && e.getMessage().contains("text segment cannot be null")) {
                        errorMsg = "No notes found. Please create and save some notes first before using the AI chat.";
                    }
                    addChatMessage("System", errorMsg, false);
                    chatInputField.setDisable(false);
                    chatSendButton.setDisable(false);
                    chatLoadingIndicator.setVisible(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void addChatMessage(String sender, String message, boolean isLoading) {
        VBox messageBox = new VBox(5);
        messageBox.setPadding(new Insets(10));
        
        String styleClass = sender.equals("You") ? "chat-message-user" : 
                          sender.equals("AI") ? "chat-message-ai" : "chat-message-system";
        messageBox.getStyleClass().add(styleClass);

        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("chat-sender-label");
        senderLabel.setStyle(senderLabel.getStyle() + 
            "-fx-font-family: 'Segoe UI', 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");

        // Use WebView to render markdown for AI messages, Label for user messages
        if (sender.equals("AI") && (message.contains("*") || message.contains("#") || message.contains("`") || message.contains("-"))) {
            WebView messageView = new WebView();
            messageView.setPrefHeight(50); // Start with smaller height
            messageView.setMinHeight(50);
            messageView.setMaxWidth(300);
            
            // Disable scrollbars
            messageView.getEngine().setJavaScriptEnabled(true);
            
            // Parse and render markdown
            Node document = parser.parse(message);
            String html = renderer.render(document);
            
            // Get background color based on sender
            String bgColor = sender.equals("You") ? "#31748f" : 
                           sender.equals("AI") ? "#26233a" : "#f6c177";
            
            String wrappedHtml = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>" +
                    "body { " +
                    "  font-family: 'Segoe UI', 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; " +
                    "  font-size: 12px; " +
                    "  line-height: 1.5; " +
                    "  padding: 8px; " +
                    "  margin: 0; " +
                    "  background-color: " + bgColor + "; " +
                    "  color: #e0def4; " +
                    "  overflow-x: hidden; " +
                    "  overflow-y: hidden; " +
                    "}" +
                    "p { margin: 4px 0; }" +
                    "h1, h2, h3, h4, h5, h6 { " +
                    "  color: #ebbcba; " +
                    "  margin: 8px 0 4px 0; " +
                    "  font-size: 13px; " +
                    "}" +
                    "code { " +
                    "  background-color: rgba(0,0,0,0.3); " +
                    "  color: #c4a7e7; " +
                    "  padding: 1px 4px; " +
                    "  border-radius: 3px; " +
                    "  font-family: 'Consolas', 'Monaco', monospace; " +
                    "  font-size: 11px; " +
                    "}" +
                    "pre { " +
                    "  background-color: rgba(0,0,0,0.3); " +
                    "  padding: 8px; " +
                    "  border-radius: 4px; " +
                    "  overflow-x: auto; " +
                    "  margin: 4px 0; " +
                    "}" +
                    "pre code { " +
                    "  background-color: transparent; " +
                    "  padding: 0; " +
                    "}" +
                    "ul, ol { " +
                    "  margin: 4px 0; " +
                    "  padding-left: 20px; " +
                    "}" +
                    "li { margin: 2px 0; }" +
                    "a { " +
                    "  color: #9ccfd8; " +
                    "  text-decoration: none; " +
                    "}" +
                    "blockquote { " +
                    "  border-left: 3px solid #ebbcba; " +
                    "  padding-left: 8px; " +
                    "  margin: 4px 0; " +
                    "  color: #908caa; " +
                    "}" +
                    "strong { color: #ebbcba; }" +
                    "em { color: #f6c177; }" +
                    "::-webkit-scrollbar { width: 0px; height: 0px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" + html + "</body>" +
                    "</html>";
            
            messageView.getEngine().loadContent(wrappedHtml, "text/html");
            
            // Adjust height based on actual content after loading
            messageView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    Platform.runLater(() -> {
                        try {
                            // Get the actual height of the content
                            Object heightObj = messageView.getEngine().executeScript(
                                "Math.max(document.body.scrollHeight, document.body.offsetHeight, " +
                                "document.documentElement.clientHeight, document.documentElement.scrollHeight, " +
                                "document.documentElement.offsetHeight);"
                            );
                            
                            if (heightObj instanceof Number) {
                                double contentHeight = ((Number) heightObj).doubleValue();
                                // Add padding and set height (min 50, max 400)
                                double finalHeight = Math.max(50, Math.min(contentHeight + 20, 400));
                                messageView.setPrefHeight(finalHeight);
                                messageView.setMinHeight(finalHeight);
                                messageView.setMaxHeight(finalHeight);
                            }
                        } catch (Exception e) {
                            // Keep default height on error
                            messageView.setPrefHeight(50);
                        }
                    });
                }
            });
            
            messageBox.getChildren().addAll(senderLabel, messageView);
        } else {
            // Use Label for plain text messages
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(300);
            messageLabel.getStyleClass().add("chat-message-text");
            messageLabel.setStyle(messageLabel.getStyle() + 
                "-fx-font-family: 'Segoe UI', 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");
            
            messageBox.getChildren().addAll(senderLabel, messageLabel);
        }
        
        // Fade in animation
        messageBox.setOpacity(0);
        chatMessagesContainer.getChildren().add(messageBox);
        
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), messageBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void clearChat() {
        chatMessagesContainer.getChildren().clear();
        addChatMessage("AI", "Chat cleared. How can I help you?", false);
    }

    private void toggleChatSidebar() {
        chatSidebarVisible = !chatSidebarVisible;
        if (chatSidebarVisible) {
            if (!mainSplitPane.getItems().contains(chatSidebar)) {
                mainSplitPane.getItems().add(chatSidebar);
                if (sideBarVisible) {
                    mainSplitPane.setDividerPositions(0.15, 0.65, 0.85);
                } else {
                    mainSplitPane.setDividerPositions(0.2, 0.8);
                }
            }
        } else {
            mainSplitPane.getItems().remove(chatSidebar);
            if (sideBarVisible) {
                mainSplitPane.setDividerPositions(0.2, 0.8);
            } else {
                mainSplitPane.setDividerPositions(0.2);
            }
        }
    }
}