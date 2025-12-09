import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import type { AppUser } from "@/api/generated/openAPIDefinition.schemas.ts";
import { UserRound } from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu.tsx";
import { useLocation, useNavigate, useParams } from "react-router";


type UserMenuProps = {
    avatarUrl: string;
    userSettings: AppUser["userSettings"];
}


export default function UserMenu( { avatarUrl, userSettings }: Readonly<UserMenuProps> ) {

    const navigate = useNavigate();
    const pathname = useLocation();
    const params = useParams();

    console.log( ( pathname ) )
    console.log( params )

    const logout = () => {
        const host = globalThis.location.host === 'localhost:5173' ? 'http://localhost:8080' : globalThis.location.origin

        window.open( host + '/logout', '_self' )
    }

    const userMenuButtons = [
        {
            label: "Profile",
            action: () => {
                navigate( "/profile" );
            },
        },
        {
            label: "Settings",
            action: () => {
                navigate( "/settings" );
            },
        },
        {
            label: "Logout",
            action: () => {
                logout()
            },
        }
    ];

    return (
        <DropdownMenu>
            <DropdownMenuTrigger>
                <Avatar className={ `${ userSettings.showAvatar ? "" : "ring" }` }>
                    { userSettings.showAvatar ?
                        <AvatarImage src={ avatarUrl } alt={ "test" }/>
                        :
                        <AvatarFallback>
                            <UserRound className={ "" }/>
                        </AvatarFallback>
                    }
                </Avatar>
            </DropdownMenuTrigger>
            <DropdownMenuContent align={ "end" } className={ "flex flex-col" }>
                { userMenuButtons.map( ( { label, action } ) => (
                    <DropdownMenuItem key={ label } onClick={ action }
                                      className={ `${ label === "Logout" && "bg-destructive/40 mt-2 text-xs hover:ring-destructive/40 hover:ring" }` }>
                        { label }
                    </DropdownMenuItem>
                ) ) }

            </DropdownMenuContent>

        </DropdownMenu>
    )
}
