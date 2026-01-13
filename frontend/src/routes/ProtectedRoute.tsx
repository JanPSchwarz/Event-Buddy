import { Navigate, Outlet } from "react-router";
import { useContextUser } from "@/context/UserProvider.tsx";
import CustomLoader from "@/components/CustomLoader.tsx";
import { toast } from "sonner";

export default function ProtectedRoute() {


    const { isLoading, isLoggedIn } = useContextUser();

    if ( isLoading ) return <CustomLoader size={ "size-6" } text={ "Loading..." }/>;

    if ( !isLoggedIn ) {
        toast.info( "You must be logged in to access this page." );
        return <Navigate to={ `/` }/>;
    }


    return <Outlet/>
}
