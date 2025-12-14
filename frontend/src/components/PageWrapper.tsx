import React from "react";
import { cn } from "@/lib/utils.ts";

type PageWrapperProps = {
    children: React.ReactNode;
    className?: string;
}

export default function PageWrapper( { children, className }: Readonly<PageWrapperProps> ) {

    return (
        <div className={ cn( "flex flex-col w-full gap-8 mt-8 justify-start items-center", className ) }>
            { children }
        </div>
    )
}
