import { Spinner } from "@/components/ui/spinner.tsx";
import { cn } from "@/lib/utils.ts";

export default function CustomLoader( { className, text, size }: Readonly<{
    className?: string,
    text?: string,
    size?: string
}> ) {

    return (
        <div className={ cn( "flex flex-col justify-center items-center gap-4", className ) }>
            <Spinner className={ size }/>
            <p className={ "" }>{ text ?? "Loading..." }</p>
        </div>
    )
}
