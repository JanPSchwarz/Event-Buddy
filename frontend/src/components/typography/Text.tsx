import { cn } from "@/lib/utils.ts";
import React from "react";


type TagTypes = "h1" | "h2" | "h3" | "h4" | "h5" | "h6" | "p" | "span";

type StyleVariants = "h1" | "h2" | "h3" | "h4" | "h5" | "h6" | "default" | "muted" | "smallMuted";

type TextProps = {
    asTag?: TagTypes,
    children?: React.ReactNode
    styleVariant?: StyleVariants
    className?: string
}

const StyleVariantClasses = [
    {
        variant: "h1",
        className: "text-4xl font-bold tracking-tight"
    },
    {
        variant: "h2",
        className: "text-3xl font-bold tracking-tight"
    },
    {
        variant: "h3",
        className: "text-2xl font-semibold tracking-tight"
    },
    {
        variant: "h4",
        className: "text-xl font-semibold tracking-tight"
    },
    {
        variant: "h5",
        className: "text-lg font-medium tracking-tight"
    },
    {
        variant: "h6",
        className: "text-base font-medium tracking-tight"
    },
    {
        variant: "default",
        className: "text-base leading-7"
    },
    {
        variant: "muted",
        className: "text-base text-muted-foreground leading-7"
    },
    {
        variant: "smallMuted",
        className: "text-sm text-muted-foreground leading-7"
    }
]


export default function Text( { asTag, children, styleVariant, className, ...props }: TextProps ) {

    const Tag = asTag || "p"

    const styles = StyleVariantClasses.find( ( style ) => style.variant === styleVariant )?.className || StyleVariantClasses.find( ( style ) => style.variant === "default" )?.className;

    return (
        <Tag
            className={ cn( "", styles, className ) }
            { ...props }>
            { children }
        </Tag>
    )
}
