import { Spinner } from "@/components/ui/spinner.tsx";
import LoginDialog from "@/components/header/LoginDialog.tsx";
import UserMenu from "@/components/header/UserMenu.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";


export default function UserHandler() {

    const { user, isLoading, isLoggedIn } = useContextUser();

    if ( isLoading ) return <Spinner className={ "size-4 text-muted-foreground" }/>;


    return (
        <div className={ "flex justify-center items-center" }>
            { isLoggedIn ?
                <UserMenu
                    userId={ user.id }
                    avatarUrl={ user.avatarUrl || "" }/>
                :
                <LoginDialog/>
            }
        </div>
    )
}
