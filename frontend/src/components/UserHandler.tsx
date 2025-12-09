import { Spinner } from "@/components/ui/spinner.tsx";
import { useGetMe } from "@/api/generated/authentication/authentication.ts";
import LoginDialog from "@/components/LoginDialog.tsx";
import UserMenu from "@/components/UserMenu.tsx";


export default function UserHandler() {

    const { data: currentUser, isLoading } = useGetMe( {
        axios: {
            withCredentials: true,
        }
    } );

    if ( isLoading ) return <Spinner className={ "size-5 text-muted-foreground" }/>;

    const user = currentUser?.data;

    return (
        <div className={ "flex justify-center items-center" }>
            { user ?
                <UserMenu
                    userSettings={ user.userSettings }
                    avatarUrl={ user.avatarUrl || "" }/>
                :
                <LoginDialog/>
            }
        </div>
    )
}
