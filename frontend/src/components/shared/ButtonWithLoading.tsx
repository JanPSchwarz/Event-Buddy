import type { ComponentProps } from "react";
import type { VariantProps } from "class-variance-authority";

import { Button, buttonVariants } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";

type ButtonWithLoadingProps = {
    isLoading?: boolean;
} & VariantProps<typeof buttonVariants> &
    ComponentProps<"button">;

export default function ButtonWithLoading( {
                                               isLoading,
                                               children,
                                               variant,
                                               size,
                                               ...props
                                           }: ButtonWithLoadingProps ) {
    return (
        <Button
            variant={ variant }
            size={ size }
            disabled={ isLoading || props.disabled }
            { ...props }
        >
            <span className={ `${ isLoading && "opacity-0" }` }>
                { children }
            </span>
            <Spinner className={ `absolute ${ isLoading ? "opacity-100" : "opacity-0" }` }/>
        </Button>
    );
}