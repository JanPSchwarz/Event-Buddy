import { Button, buttonVariants } from "@/components/ui/button.tsx";
import * as React from "react";
import { useState } from "react";
import type { VariantProps } from "class-variance-authority";
import { Spinner } from "@/components/ui/spinner.tsx";

type LoginButtonProps = {
    className?: string;
    callbackUrl: string;
    size?: VariantProps<typeof buttonVariants>["size"];
    variant?: VariantProps<typeof buttonVariants>["variant"];
    children: React.ReactNode;
}

export default function LoginButton( {
                                         className,
                                         variant,
                                         size,
                                         callbackUrl,
                                         children,
                                     }: Readonly<LoginButtonProps> ) {

    const [ loading, setLoading ] = useState( false );

    const handleLogin = () => {
        setLoading( true );
        login();
    }


    const login = () => {
        const host = globalThis.location.host === "localhost:5173" ? "http://localhost:8080" : globalThis.location.origin;


        window.open( host + callbackUrl, "_self" );
    }

    return (
        <Button className={ className }
                onClick={ handleLogin }
                variant={ variant }
                disabled={ loading }
                size={ size }>
            { loading ? <Spinner/> : children }
        </Button>
    )
}
