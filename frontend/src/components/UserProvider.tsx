import { createContext, type ReactNode, useContext, useMemo } from "react";
import type { AppUser } from "@/api/generated/openAPIDefinition.schemas.ts";
import { useGetMe } from "@/api/generated/authentication/authentication.ts";

type UserProviderProps = {
    children: ReactNode
}

type UserProviderState = {
    user: AppUser | null
    isLoading: boolean
    loggedIn: boolean
}

const initialState: UserProviderState = {
    user: null,
    isLoading: true,
    loggedIn: false
}

const UserProviderContext = createContext<UserProviderState>( initialState )

export function UserProvider( {
                                  children,
                                  ...props
                              }: Readonly<UserProviderProps> ) {

    const { data: currentUser, isLoading } = useGetMe( {
        axios: {
            withCredentials: true,
        }
    } );

    const value = useMemo( () => ( {
        user: currentUser?.data || null,
        isLoading,
        loggedIn: !isLoading && !!currentUser?.data
    } ), [ currentUser, isLoading ] );

    return (
        <UserProviderContext.Provider { ...props } value={ value }>
            { children }
        </UserProviderContext.Provider>
    )
}

export const useContextUser = () => {
    const context = useContext( UserProviderContext );
    if ( context === undefined ) {
        throw new Error( "useContextUser must be used within a UserProvider" );
    }
    return context;
}