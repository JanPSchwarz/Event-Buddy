"use client"

import { useEffect, useRef, useState } from "react"
import { EditorContent, EditorContext, useEditor } from "@tiptap/react"

// --- Tiptap Core Extensions ---
import { StarterKit } from "@tiptap/starter-kit"
import { TaskItem, TaskList } from "@tiptap/extension-list"
import { TextAlign } from "@tiptap/extension-text-align"
import { Typography } from "@tiptap/extension-typography"
import { Highlight } from "@tiptap/extension-highlight"
import { Subscript } from "@tiptap/extension-subscript"
import { Superscript } from "@tiptap/extension-superscript"

// --- UI Primitives ---
import { Button } from "@/components/ui/tiptap-ui-primitive/button"
import { Spacer } from "@/components/ui/tiptap-ui-primitive/spacer"
import { Toolbar, ToolbarGroup, ToolbarSeparator, } from "@/components/ui/tiptap-ui-primitive/toolbar"

// --- Tiptap Node ---
import { HorizontalRule } from "@/components/ui/tiptap-node/horizontal-rule-node/horizontal-rule-node-extension.ts"
import "@/components/ui/tiptap-node/blockquote-node/blockquote-node.scss"
import "@/components/ui/tiptap-node/code-block-node/code-block-node.scss"
import "@/components/ui/tiptap-node/horizontal-rule-node/horizontal-rule-node.scss"
import "@/components/ui/tiptap-node/list-node/list-node.scss"
import "@/components/ui/tiptap-node/image-node/image-node.scss"
import "@/components/ui/tiptap-node/heading-node/heading-node.scss"
import "@/components/ui/tiptap-node/paragraph-node/paragraph-node.scss"

// --- Tiptap UI ---
import { HeadingDropdownMenu } from "@/components/ui/tiptap-ui/heading-dropdown-menu"
import { ListDropdownMenu } from "@/components/ui/tiptap-ui/list-dropdown-menu"
import { BlockquoteButton } from "@/components/ui/tiptap-ui/blockquote-button"
import {
    ColorHighlightPopover,
    ColorHighlightPopoverButton,
    ColorHighlightPopoverContent,
} from "@/components/ui/tiptap-ui/color-highlight-popover"
import { LinkButton, LinkContent, LinkPopover, } from "@/components/ui/tiptap-ui/link-popover"
import { MarkButton } from "@/components/ui/tiptap-ui/mark-button"
import { TextAlignButton } from "@/components/ui/tiptap-ui/text-align-button"
import { UndoRedoButton } from "@/components/ui/tiptap-ui/undo-redo-button"

// --- Icons ---
import { ArrowLeftIcon } from "@/components/ui/tiptap-icons/arrow-left-icon.tsx"
import { HighlighterIcon } from "@/components/ui/tiptap-icons/highlighter-icon.tsx"
import { LinkIcon } from "@/components/ui/tiptap-icons/link-icon.tsx"

// --- Hooks ---
import { useIsBreakpoint } from "@/hooks/use-is-breakpoint.ts"

// --- Components ---
// --- Lib ---
// --- Styles ---
import "@/components/ui/tiptap-templates/simple/simple-editor.scss"

import placeholderContent from "@/components/ui/tiptap-templates/simple/data/placeholderContent.json"
import type { ControllerRenderProps } from "react-hook-form";

const MainToolbarContent = ( {
                                 onHighlighterClick,
                                 onLinkClick,
                                 isMobile,
                             }: {
    onHighlighterClick: () => void
    onLinkClick: () => void
    isMobile: boolean
} ) => {
    return (
        <>
            <Spacer/>

            <ToolbarGroup>
                <UndoRedoButton action="undo"/>
                <UndoRedoButton action="redo"/>
            </ToolbarGroup>

            <ToolbarSeparator/>

            <ToolbarGroup>
                <HeadingDropdownMenu levels={ [ 1, 2, 3, 4 ] } portal={ true }/>
                <ListDropdownMenu
                    types={ [ "bulletList", "orderedList", "taskList" ] }
                    portal={ true }
                />
                <BlockquoteButton/>
            </ToolbarGroup>

            <ToolbarSeparator/>

            <ToolbarGroup>
                <MarkButton type="bold"/>
                <MarkButton type="italic"/>
                <MarkButton type="strike"/>
                <MarkButton type="underline"/>
                { isMobile ? (
                    <ColorHighlightPopoverButton onClick={ onHighlighterClick }/>
                ) : (
                    <ColorHighlightPopover/>
                ) }
                { isMobile ? <LinkButton onClick={ onLinkClick }/> : <LinkPopover/> }
            </ToolbarGroup>

            <ToolbarSeparator/>

            <ToolbarGroup>
                <MarkButton type="superscript"/>
                <MarkButton type="subscript"/>
            </ToolbarGroup>

            <ToolbarSeparator/>

            <ToolbarGroup>
                <TextAlignButton align="left"/>
                <TextAlignButton align="center"/>
                <TextAlignButton align="right"/>
                <TextAlignButton align="justify"/>
            </ToolbarGroup>

            <ToolbarSeparator/>


            <Spacer/>
        </>
    )
}

const MobileToolbarContent = ( {
                                   type,
                                   onBack,
                               }: {
    type: "highlighter" | "link"
    onBack: () => void
} ) => (
    <>
        <ToolbarGroup>
            <Button data-style="ghost" onClick={ onBack }>
                <ArrowLeftIcon className="tiptap-button-icon"/>
                { type === "highlighter" ? (
                    <HighlighterIcon className="tiptap-button-icon"/>
                ) : (
                    <LinkIcon className="tiptap-button-icon"/>
                ) }
            </Button>
        </ToolbarGroup>

        <ToolbarSeparator/>

        { type === "highlighter" ? (
            <ColorHighlightPopoverContent/>
        ) : (
            <LinkContent/>
        ) }
    </>
)

type SimpleEditorProps = {
    field: ControllerRenderProps<any, any>,
    initialContent?: string,
}

export function SimpleEditor( { field, initialContent }: Readonly<SimpleEditorProps> ) {
    const isMobile = useIsBreakpoint()
    const [ mobileView, setMobileView ] = useState<"main" | "highlighter" | "link">(
        "main"
    )
    const toolbarRef = useRef<HTMLDivElement>( null )

    const editor = useEditor( {
        immediatelyRender: false,
        onUpdate: ( editor ) => {
            const html = editor.editor.getHTML();
            field.onChange( html );
        },
        editorProps: {
            attributes: {
                autocomplete: "off",
                autocorrect: "off",
                autocapitalize: "off",
                "aria-label": "Main content area, start typing to enter text.",
                class: "simple-editor",
            },
        },
        extensions: [
            StarterKit.configure( {
                horizontalRule: false,
                link: {
                    openOnClick: false,
                    defaultProtocol: 'https',
                    protocols: [ 'http', 'https' ],
                    enableClickSelection: true,
                },
            } ),
            HorizontalRule,
            TextAlign.configure( { types: [ "heading", "paragraph" ] } ),
            TaskList,
            TaskItem.configure( { nested: true } ),
            Highlight.configure( { multicolor: true } ),
            Typography,
            Superscript,
            Subscript,
        ],
        content: initialContent || placeholderContent,
    } )

    useEffect( () => {
        if ( !isMobile && mobileView !== "main" ) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setMobileView( "main" )
        }
    }, [ isMobile, mobileView ] )

    return (
        <div className="simple-editor-wrapper">
            <EditorContext.Provider value={ { editor } }>
                <Toolbar
                    ref={ toolbarRef }
                    className={ "bg-muted rounded-md" }
                >
                    { mobileView === "main" ? (
                        <MainToolbarContent
                            onHighlighterClick={ () => setMobileView( "highlighter" ) }
                            onLinkClick={ () => setMobileView( "link" ) }
                            isMobile={ isMobile }
                        />
                    ) : (
                        <MobileToolbarContent
                            type={ mobileView === "highlighter" ? "highlighter" : "link" }
                            onBack={ () => setMobileView( "main" ) }
                        />
                    ) }
                </Toolbar>

                <EditorContent
                    editor={ editor }
                    role="presentation"
                    className="simple-editor-content"
                />
            </EditorContext.Provider>
        </div>
    )
}
