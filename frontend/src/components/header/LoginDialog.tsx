import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog.tsx";
import { Button } from "@/components/ui/button.tsx";
import { loginProvider } from "@/lib/loginProvider.tsx";
import LoginButton from "@/components/header/LoginButton.tsx";
import { useSearchParams } from "react-router";

export default function LoginDialog() {

    const [ searchParams, setSearchParams ] = useSearchParams();

    const isLoginModal = searchParams.get( "loginModal" ) === "true";

    const handleOpenChange = ( open: boolean ) => {
        if ( open ) {
            setSearchParams( ( searchParams ) => {
                searchParams.set( "loginModal", "true" );
                return searchParams;
            } );
        } else {
            setSearchParams( ( searchParams ) => {
                searchParams.delete( "loginModal" );
                return searchParams;
            } );
        }
    }

    return (
        <Dialog open={ isLoginModal } onOpenChange={ handleOpenChange }>
            <DialogTrigger asChild>
                <Button className={ "h-8 px-3 md:h-9 md:px-6 text-sm md:text-base" }>
                    Login
                </Button>
            </DialogTrigger>
            <DialogContent className={ "space-y-4" }>
                <DialogHeader>
                    <DialogTitle>Login</DialogTitle>
                    <DialogDescription>
                        Login with a provider of your choice.
                    </DialogDescription>
                </DialogHeader>
                <div className={ "flex justify-center" }>
                    { loginProvider
                        .map( ( { provider, callbackUrl, logoSrc } ) => (
                            <LoginButton
                                key={ provider }
                                callbackUrl={ callbackUrl }
                                variant={ "secondary" }
                                className={ "flex w-76 gap-4 bg-slate-50 text-stone-800 hover:border border-foreground border hover:border-primary hover:bg-slate/80" }>
                                { provider }
                                <img src={ logoSrc } alt={ "logo" } className={ "size-6" }/>
                            </LoginButton>
                        ) )
                    }
                </div>
            </DialogContent>
        </Dialog>
    )
}
