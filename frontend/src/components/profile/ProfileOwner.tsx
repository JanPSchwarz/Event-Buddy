import { useState } from "react";
import { useContextUser } from "@/context/UserProvider.tsx";
import { toast } from "sonner";
import { Navigate, NavLink, useNavigate } from "react-router";
import ProfileView from "@/components/profile/ProfileView.tsx";
import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Alert, AlertDescription } from "@/components/ui/alert.tsx";
import { Info } from "lucide-react";
import EditProfile from "@/components/profile/EditProfile.tsx";


export default function ProfileOwner() {

    const [ showOwnerView, setShowOwnerView ] = useState( true );

    const [ isEditing, setIsEditing ] = useState( false );

    const navigate = useNavigate();

    const { user: loggedInUser, isLoggedIn } = useContextUser();

    if ( !isLoggedIn ) {
        toast.error( "User not logged in" );
        return <Navigate to={ `/` }/>;
    }

    const toggleWatchMode = () => {
        if ( isEditing ) return;

        setShowOwnerView( !showOwnerView );
    }

    const toggleIsEdditing = () => {
        setIsEditing( !isEditing );

        if ( !showOwnerView ) setShowOwnerView( true );
    }

    const { showAvatar, showEmail, userVisible } = loggedInUser.userSettings;

    const displayData = showOwnerView ?
        loggedInUser
        :
        {
            ...loggedInUser,
            avatarUrl: showAvatar ? loggedInUser.avatarUrl : undefined,
            email: showEmail ? loggedInUser.email : undefined,
        }

    const handleGoToSettings = () => {
        if ( isEditing ) return;
        navigate( `/settings/${ loggedInUser.id }?fromProfilePage=true` );
    }


    return (
        <div className={ "w-full space-y-8" }>
            <div className={ "w-full flex justify-between border-b border-primary" }>
                <Text asTag={ "h1" } styleVariant={ "h1" }>
                    Your Profile
                </Text>
                <Text asTag={ "span" } styleVariant={ "smallMuted" } className={ "mt-auto" }>
                    Manage your Profile
                </Text>
            </div>
            <div className={ "w-full flex gap-2 justify-between" }>
                <div className={ "space-x-4 space-y-4" }>
                    <Button variant={ "outline" } disabled={ isEditing } onClick={ toggleWatchMode }>
                        { showOwnerView ? "Foreigner" : "Manager" } View
                    </Button>
                    <Button variant={ "outline" } disabled={ isEditing }
                            onClick={ handleGoToSettings }
                    >
                        Go to Settings
                    </Button>
                </div>
                <Button variant={ "outline" } onClick={ toggleIsEdditing }>
                    { isEditing ? "Cancel" : "Edit Profile" }
                </Button>
            </div>
            {
                !showOwnerView &&
                <Text asTag={ "h5" } styleVariant={ "h5" } className={ "italic" }>
                    How others see your profile based on your current privacy settings
                </Text>
            }
            {
                ( !showOwnerView && !userVisible ) &&
                <Alert>
                    <Info className={ "stroke-blue-400" }/>
                    <AlertDescription className={ "block" }>
                        Your profile is currently hidden from other users. To make it visible, please update
                        your { " " }
                        <NavLink to={ `/settings/${ loggedInUser.id }?fromProfilePage=true` }
                                 className={ "text-primary underline inline underline-offset-2" }>
                            settings
                        </NavLink>
                    </AlertDescription>
                </Alert>
            }
            {
                isEditing ?
                    <EditProfile userData={ loggedInUser } onSubmit={ toggleIsEdditing }/>
                    :
                    <ProfileView userData={ displayData }/>
            }

        </div>
    )
}
