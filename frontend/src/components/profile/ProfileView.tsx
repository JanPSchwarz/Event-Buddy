import { Card, CardContent } from "@/components/ui/card.tsx";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { User } from 'lucide-react';
import Text from "@/components/typography/Text.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import type { AppUserDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { NavLink } from "react-router";


type ProfileViewProps = {
    userData: AppUserDto;
}

export default function ProfileView( { userData }: Readonly<ProfileViewProps> ) {

    const listData = [
        { label: "Email", value: userData.email ? userData.email : "N/A" },
    ]

    return (
        <Card className={ "w-full max-w-[600px] mx-auto" }>
            <CardContent className={ "flex flex-col gap-4" }>
                <div className={ "flex justify-between" }>
                    <Text asTag={ "h3" } styleVariant={ "h3" }>
                        { userData.name }
                    </Text>
                    <Avatar className={ "md:size-24 size-12 border" }>
                        <AvatarImage src={ userData.avatarUrl } alt={ userData.name }/>
                        <AvatarFallback>
                            <User className={ "size-12" }/>
                        </AvatarFallback>
                    </Avatar>
                </div>
                <Separator/>
                { listData.map( ( { label, value } ) => (
                    <div key={ label } className={ "grid grid-cols-2 gap-2 items-end" }>
                        <Text asTag={ "p" } className={ "break-words" }>
                            { label }:
                        </Text>
                        <Text className={ "italic break-words" }>
                            { value }
                        </Text>
                    </div>
                ) ) }
                { userData.organizations && userData.organizations.length > 0 &&
                    <div className={ "space-y-4" }>
                        <Separator/>
                        <div className={ "grid grid-cols-2" }>
                            <Text className={ "text-muted-foreground" }>
                                Organizations
                            </Text>
                            { userData.organizations.map( ( org ) => (
                                <div key={ org.id } className={ "mb-4 last:mb-0 col-start-2" }>
                                    <NavLink to={ `/organization/${ org.slug }` }
                                             className={ "hover:underline underline-offset-4 hover:text-primary" }>
                                        <Text className={ "hover:text-primary" }>
                                            { org.name }
                                        </Text>
                                    </NavLink>
                                </div>
                            ) ) }
                        </div>

                    </div>
                }
            </CardContent>
        </Card>
    )
}
