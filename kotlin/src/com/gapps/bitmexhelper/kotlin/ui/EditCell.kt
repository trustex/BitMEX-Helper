package com.gapps.bitmexhelper.kotlin.ui

import javafx.event.Event
import javafx.scene.control.*
import javafx.scene.control.TableColumn.CellEditEvent
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent
import javafx.util.StringConverter


class EditCell<S, T>(// Converter for converting the text in the text field to the user type, and vice-versa:
        private val converter: StringConverter<T>) : TableCell<S, T>() {

    // Text field for editing
    // TODO: allow this to be a plugable control.
    private val textField = TextField()

    init {

        itemProperty().addListener { _, _, newItem ->
            if (newItem == null) {
                setText(null)
            } else {
                setText(converter.toString(newItem))
            }
        }
        graphic = textField
        contentDisplay = ContentDisplay.TEXT_ONLY

        textField.setOnAction { _ -> commitEdit(this.converter.fromString(textField.text)) }
        textField.focusedProperty().addListener { _, _, isNowFocused ->
            if (!isNowFocused) {
                commitEdit(this.converter.fromString(textField.text))
            }
        }
        textField.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            when {
                event.code == ESCAPE -> {
                    textField.text = converter.toString(item)
                    cancelEdit()
                    event.consume()
                }
                event.code == RIGHT -> {
                    tableView.selectionModel.selectRightCell()
                    event.consume()
                }
                event.code == LEFT -> {
                    tableView.selectionModel.selectLeftCell()
                    event.consume()
                }
                event.code == UP -> {
                    tableView.selectionModel.selectAboveCell()
                    event.consume()
                }
                event.code == DOWN -> {
                    tableView.selectionModel.selectBelowCell()
                    event.consume()
                }
            }
        }
    }


    // set the text of the text field and display the graphic
    override fun startEdit() {
        super.startEdit()
        textField.text = converter.toString(item)
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        textField.requestFocus()
    }

    // revert to text display
    override fun cancelEdit() {
        super.cancelEdit()
        contentDisplay = ContentDisplay.TEXT_ONLY
    }

    // commits the edit. Update property if possible and revert to text display
    override fun commitEdit(item: T?) {

        // This block is necessary to support commit on losing focus, because the baked-in mechanism
        // sets our editing state to false before we can intercept the loss of focus.
        // The default commitEdit(...) method simply bails if we are not editing...
        if (!isEditing && item != getItem()) {
            val table = tableView
            if (table != null) {
                val column = tableColumn
                val event = CellEditEvent(table,
                        TablePosition(table, index, column),
                        TableColumn.editCommitEvent(), item)
                Event.fireEvent(column, event)
            }
        }

        super.commitEdit(item)

        contentDisplay = ContentDisplay.TEXT_ONLY
    }

    companion object {

        /**
         * Convenience converter that does nothing (converts Strings to themselves and vice-versa...).
         */
        private val IDENTITY_CONVERTER: StringConverter<String> = object : StringConverter<String>() {

            override fun toString(`object`: String): String {
                return `object`
            }

            override fun fromString(string: String): String {
                return string
            }

        }

        /**
         * Convenience method for creating an EditCell for a String value.
         * @return
         */
        @Suppress("unused")
        fun <S> createStringEditCell(): EditCell<S, String> {
            return EditCell(IDENTITY_CONVERTER)
        }
    }

}