import { useContextUser } from "@/components/UserProvider.tsx";
import { Navigate, useParams } from "react-router";
import CustomLoader from "@/components/CustomLoader.tsx";
import { toast } from 'sonner';

export default function SettingsPage() {

    const { userId } = useParams();

    const { user, isLoading } = useContextUser();

    if ( isLoading ) return <CustomLoader size={ "size-6" } text={ "Settings loading..." }/>;

    if ( !user ) {
        toast.info( "You must be logged in to access settings." );
        return <Navigate to={ `/` }/>;
    }

    if ( user?.id !== userId ) {
        toast.error( "You can only access your own settings." );
        return <Navigate to={ `/` }/>
    }

    return (
        <p>
            { user?.name }
        </p>
    )
}

