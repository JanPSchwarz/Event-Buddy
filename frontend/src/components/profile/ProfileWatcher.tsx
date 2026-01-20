import { useGetUserById } from "@/api/generated/user/user.ts";
import { Navigate, useParams } from "react-router";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import { toast } from "sonner";
import ProfileView from "@/components/profile/ProfileView.tsx";
import Text from "@/components/typography/Text.tsx";
import PageNotFound from "@/pages/PageNotFound.tsx";

export default function ProfileWatcher() {

    const { userId: profileUserId } = useParams();

    const { data: fetchedProfileData, isLoading: isLoadingProfileData, error } = useGetUserById( profileUserId || "", {
        query: {
            enabled: !!profileUserId
        }
    } );

    if ( isLoadingProfileData ) {
        return <CustomLoader size={ "size-6" } text={ "Profile loading..." }/>
    }

    if ( error && error.response?.status !== 404 ) {
        toast.error( error.response?.data.error || error.message || "Failed to load profile." );
        return <Navigate to={ "/" }/>
    }

    return (
        <>
            {
                fetchedProfileData?.data ?
                    <div className={ "w-full space-y-12" }>
                        <div className={ "border-b flex justify-between border-primary" }>
                            <Text asTag={ "h1" } styleVariant={ "h1" }>
                                Profile Page
                            </Text>
                            <Text styleVariant={ "smallMuted" } className={ "mt-auto" }>
                                of { fetchedProfileData.data.name }
                            </Text>
                        </div>
                        <ProfileView userData={ fetchedProfileData.data }/>
                    </div>
                    :
                    <div className={ "w-full flex flex-col justify-center items-center gap-12" }>
                        <Text asTag={ "h1" } styleVariant={ "h3" }
                              className={ "text-primary border-b border-muted-foreground" }>The user you are looking for
                            does not exist</Text>
                        <PageNotFound/>
                    </div>
            }
        </>
    )
}
