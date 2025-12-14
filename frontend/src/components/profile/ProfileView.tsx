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
        <Card className={ "w-11/12 max-w-[600px] mx-auto mt-12" }>
            <CardContent className={ "flex flex-col gap-4" }>
                <div className={ "flex justify-between" }>
                    <Text asTag={ "h2" } styleVariant={ "h2" }>
                        { userData.name }
                    </Text>
                    <Avatar className={ "size-24" }>
                        <AvatarImage src={ userData.avatarUrl } alt={ userData.name }/>
                        <AvatarFallback>
                            <User className={ "size-12" }/>
                        </AvatarFallback>
                    </Avatar>
                </div>
                <Separator/>
                { listData.map( ( { label, value } ) => (
                    <div key={ label } className={ "grid grid-cols-2" }>
                        <Text asTag={ "p" }>
                            { label }:
                        </Text>
                        <Text className={ "italic" }>
                            { value }
                        </Text>
                    </div>
                ) ) }
            </CardContent>
        </Card>
    )
}
