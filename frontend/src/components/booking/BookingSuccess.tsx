import type { EventResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { CircleCheck } from "lucide-react";
import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";
import { useContextUser } from "@/context/UserProvider.tsx";

type BookingSuccessProps = {
    event: EventResponseDto
}

export default function BookingSuccess( { event }: Readonly<BookingSuccessProps> ) {

    const { user } = useContextUser();

    return (
        <div className={ "w-full flex flex-col gap-8 justify-center items-center" }>
            <CircleCheck className={ "size-24 text-green-500" }/>
            <div className={ "flex flex-col items-center" }>
                <Text>
                    Your booking for
                </Text>
                <Text styleVariant={ "h4" } className={ "my-4 text-primary" }>
                    { event.title }
                </Text>
                <Text>
                    was successful!
                </Text>
            </div>
            <Button asChild>
                <NavLink to={ `/dashboard/${ user.id }` }>
                    Go to Dashboard
                </NavLink>
            </Button>
        </div>
    )
}
