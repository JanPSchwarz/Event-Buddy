import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { UserRound } from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu.tsx";
import { useLocation, useNavigate } from "react-router";


type UserMenuProps = {
    avatarUrl: string;
    userId: string;
}


export default function UserMenu( { avatarUrl, userId }: Readonly<UserMenuProps> ) {

    const navigate = useNavigate();
    const { pathname } = useLocation();


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
                navigate( `/settings/${ userId }` );
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
                <Avatar className={ `${ avatarUrl ? "" : "ring" }` }>
                    <AvatarImage src={ avatarUrl } alt={ "test" }/>
                    <AvatarFallback>
                        <UserRound className={ "" }/>
                    </AvatarFallback>
                </Avatar>
            </DropdownMenuTrigger>
            <DropdownMenuContent align={ "end" } className={ "flex flex-col" }>
                { userMenuButtons.map( ( { label, action } ) => (
                    <DropdownMenuItem key={ label }
                                      onClick={ action }
                                      className={ `flex justify-between ${ label === "Logout" && "bg-destructive/40 mt-2 text-xs hover:ring-destructive/40 hover:ring" }` }>
                        { label }
                        { pathname.split( "/" ).includes( `${ label.toLowerCase() }` ) &&
                            <div className={ "rounded-full size-2 bg-green-400/60" }/>
                        }
                    </DropdownMenuItem>
                ) ) }

            </DropdownMenuContent>

        </DropdownMenu>
    )
}
