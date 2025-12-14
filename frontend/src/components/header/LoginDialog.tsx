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

export default function LoginDialog() {

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button>Login</Button>
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
                                className={ "flex w-76 gap-4 hover:bg-foreground/80 bg-foreground text-background" }>
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
