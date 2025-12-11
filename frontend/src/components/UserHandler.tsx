import { Spinner } from "@/components/ui/spinner.tsx";
import LoginDialog from "@/components/LoginDialog.tsx";
import UserMenu from "@/components/UserMenu.tsx";
import { useContextUser } from "@/components/UserProvider.tsx";


export default function UserHandler() {

    const { user, isLoading } = useContextUser();

    if ( isLoading ) return <Spinner className={ "size-4 text-muted-foreground" }/>;


    return (
        <div className={ "flex justify-center items-center" }>
            { user ?
                <UserMenu
                    userSettings={ user.userSettings }
                    userId={ user.id }
                    avatarUrl={ user.avatarUrl || "" }/>
                :
                <LoginDialog/>
            }
        </div>
    )
}
