package com.notia;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class App extends Application {

    private enum ViewState {
        PREVIEW,
        EDIT,
        SPLIT
    }

    private TextArea markdownEditor;
    private WebView htmlViewer;
    private ListView<Note> noteList;
    private ObservableList<Note> notes;
    private Note currentNote;
    private BorderPane centerPane;
    private SplitPane splitPane;

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
        htmlViewer = new WebView();
        noteList = new ListView<>();
        centerPane = new BorderPane();
        splitPane = new SplitPane(markdownEditor, htmlViewer);

        notes = FXCollections.observableArrayList(Database.getAllNotes());
        noteList.setItems(notes);

        noteList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadNoteContent(newValue);
            }
        });

        markdownEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            updateHtmlView(newValue);
        });

        ToolBar toolBar = createToolBar();

        SplitPane mainSplitPane = new SplitPane(noteList, centerPane);
        mainSplitPane.setDividerPositions(0.25);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(mainSplitPane);

        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("Notia - Markdown Note Taking");
        stage.setScene(scene);
        stage.show();

        updateView();
    }

    private ToolBar createToolBar() {
        Button newNoteButton = new Button("New Note");
        newNoteButton.setOnAction(e -> createNewNote());

        Button saveNoteButton = new Button("Save Note");
        saveNoteButton.setOnAction(e -> saveCurrentNote());

        Button deleteNoteButton = new Button("Delete Note");
        deleteNoteButton.setOnAction(e -> deleteCurrentNote());

        Button previewButton = new Button("Preview");
        previewButton.setOnAction(e -> {
            currentView = ViewState.PREVIEW;
            updateView();
        });

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            currentView = ViewState.EDIT;
            updateView();
        });

        Button splitViewButton = new Button("Split View");
        splitViewButton.setOnAction(e -> {
            currentView = ViewState.SPLIT;
            updateView();
        });

        return new ToolBar(newNoteButton, saveNoteButton, deleteNoteButton, new Separator(), previewButton, editButton, splitViewButton);
    }

    private void createNewNote() {
        currentNote = null;
        markdownEditor.clear();
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
            currentNote = new Note(0, title, content);
        } else {
            currentNote = new Note(currentNote.getId(), title, content);
        }

        int noteId = Database.saveNote(currentNote);
        if (noteId != 0) {
            currentNote = new Note(noteId, title, content);
            refreshNoteList();
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
        htmlViewer.getEngine().loadContent(html);
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
}