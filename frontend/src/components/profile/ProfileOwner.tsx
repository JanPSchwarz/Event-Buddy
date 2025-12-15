import { useState } from "react";
import { useContextUser } from "@/context/UserProvider.tsx";
import { toast } from "sonner";
import { Navigate, NavLink } from "react-router";
import ProfileView from "@/components/profile/ProfileView.tsx";
import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Alert, AlertDescription } from "@/components/ui/alert.tsx";
import { Info } from "lucide-react";


export default function ProfileOwner() {

    const [ showOwnerView, setShowOwnerView ] = useState( true );

    const { user: loggedInUser, isLoggedIn } = useContextUser();

    if ( !isLoggedIn ) {
        toast.error( "User not logged in" );
        return <Navigate to={ `/` }/>;
    }

    const toggleWatchMode = () => {
        setShowOwnerView( !showOwnerView );
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


    return (
        <div className={ "w-11/12 space-y-8" }>
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
                    <Button variant={ "outline" } onClick={ toggleWatchMode }>
                        { showOwnerView ? "Foreigner" : "Manager" } View
                    </Button>
                    <Button variant={ "outline" } asChild onClick={ toggleWatchMode }>
                        <NavLink to={ `/settings/${ loggedInUser.id }?fromProfilePage=true` }>
                            Go to Settings
                        </NavLink>
                    </Button>
                </div>
                <Button variant={ "outline" }>Edit Profile</Button>
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
            <ProfileView userData={ displayData }/>
        </div>
    )
}
