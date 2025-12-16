import { Card, CardContent } from "@/components/ui/card.tsx";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { User } from 'lucide-react';
import Text from "@/components/typography/Text.tsx";
import { Separator } from "@/components/ui/separator.tsx";


type UserDataType = {
    name: string;
    email?: string;
    avatarUrl?: string;
}

type ProfileViewProps = {
    userData: UserDataType;
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
            </CardContent>
        </Card>
    )
}
