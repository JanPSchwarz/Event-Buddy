import { useContextUser } from "@/context/UserProvider.tsx";
import { Navigate, NavLink, useParams } from "react-router";
import { toast } from 'sonner';
import SettingsForm from "@/components/settings/SettingsForm.tsx";
import { Settings } from 'lucide-react';
import Text from "@/components/typography/Text.tsx";
import PageWrapper from "@/components/PageWrapper.tsx";
import { Button } from "@/components/ui/button.tsx";


export default function SettingsPage() {

    const { userId } = useParams();

    const { user } = useContextUser();

    if ( user.id !== userId ) {
        toast.info( "You can only access your own settings." );
        return <Navigate to={ `/` }/>
    }

    return (
        <PageWrapper>
            <div className={ "flex w-full gap-4 flex-col items-center" }>
                <div
                    className={ "p-2 gap-2 md:flex-row w-full md:gap-0 border-b border-primary/60 flex tracking-tight text-balance flex-col justify-between" }>
                    <Text asTag={ "h1" } styleVariant={ "h1" }>
                        Settings <Settings className={ "inline -translate-y-1/2" }/>
                    </Text>
                    <Text asTag={ "span" } styleVariant={ "smallMuted" } className={ "mt-auto" }>
                        Manage your settings
                    </Text>
                </div>
                <div className={ "flex justify-end w-full" }>
                    <Button variant={ "outline" } asChild>
                        <NavLink to={ `/profile/${ user.id }` }>
                            Go to Profile
                        </NavLink>
                    </Button>
                </div>
            </div>
            <SettingsForm/>
        </PageWrapper>
    )
}

