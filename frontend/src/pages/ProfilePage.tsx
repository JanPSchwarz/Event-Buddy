import { useParams } from "react-router";
import { useContextUser } from "@/context/UserProvider.tsx";
import CustomLoader from "@/components/CustomLoader.tsx";
import PageWrapper from "@/components/PageWrapper.tsx";
import ProfileOwner from "@/components/profile/ProfileOwner.tsx";
import ProfileWatcher from "@/components/profile/ProfileWatcher.tsx";

export default function ProfilePage() {

    const { userId: profileUserId } = useParams();

    const { user: loggedInUser, isLoading: isLoadingLoggedInUser } = useContextUser();

    if ( isLoadingLoggedInUser ) {
        return <CustomLoader size={ "size-6" } text={ "Profile loading..." }/>
    }

    const isOwner = loggedInUser.id === profileUserId;

    return (
        <PageWrapper>
            { isOwner ?
                <ProfileOwner/>
                :
                <ProfileWatcher/>
            }
        </PageWrapper>
    )
}
