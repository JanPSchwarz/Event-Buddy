import { EditorContent, useEditor } from "@tiptap/react";
import { StarterKit } from "@tiptap/starter-kit";
import HorizontalRule from "@tiptap/extension-horizontal-rule";
import { TextAlign } from "@tiptap/extension-text-align";
import { TaskItem, TaskList } from "@tiptap/extension-list";
import { Typography } from "@tiptap/extension-typography";
import { Superscript } from "@tiptap/extension-superscript";
import { Subscript } from "@tiptap/extension-subscript";
import { Highlight } from "@tiptap/extension-highlight"

type RichTextProps = {
    content?: string
}

export default function RichText( { content = '' }: Readonly<RichTextProps> ) {

    const editor = useEditor( {
            shouldRerenderOnTransaction: false,
            editable: false,
            content: content,
            extensions: [
                StarterKit.configure( {
                    horizontalRule: false,
                } ),
                HorizontalRule,
                TextAlign.configure( { types: [ "heading", "paragraph" ] } ),
                TaskList,
                TaskItem.configure( { nested: true } ),
                Highlight.configure( { multicolor: true } ),
                Typography,
                Superscript,
                Subscript,
            ]
        }
    )
    if ( !editor ) {
        return null;
    }

    return (
        <EditorContent editor={ editor }/>
    )
}
