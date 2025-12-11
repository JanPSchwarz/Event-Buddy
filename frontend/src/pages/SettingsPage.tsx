import { useContextUser } from "@/context/UserProvider.tsx";
import { Navigate, useParams } from "react-router";
import CustomLoader from "@/components/CustomLoader.tsx";
import { toast } from 'sonner';
import SettingsForm from "@/components/SettingsForm.tsx";
import { Settings } from 'lucide-react';


export default function SettingsPage() {

    const { userId } = useParams();

    const { user, isLoading, isLoggedIn } = useContextUser();

    if ( isLoading ) return <CustomLoader size={ "size-6" } text={ "Settings loading..." }/>;

    if ( !isLoggedIn ) {
        toast.info( "You must be logged in to access settings." );
        return <Navigate to={ `/` }/>;
    }

    if ( user.id !== userId ) {
        toast.info( "You can only access your own settings." );
        return <Navigate to={ `/` }/>
    }

    return (
        <div className={ "flex flex-col w-full  gap-8 mt-8 justify-start items-center" }>
            <div
                className={ "w-11/12 p-2 gap-2 md:flex-row md:gap-0 border-b border-primary/60 flex tracking-tight text-balance flex-col justify-between" }>
                <h2 className={ " w-full text-left text-4xl font-extrabold " }>
                    Settings <Settings className={ "inline -translate-y-1/2" }/>
                </h2>
                <span className={ "text-nowrap text-sm text-muted-foreground mt-auto" }>Change your settings</span>
            </div>
            <SettingsForm/>
        </div>
    )
}

