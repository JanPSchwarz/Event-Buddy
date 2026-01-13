import Text from "@/components/typography/Text.tsx";
import { twMerge } from "tailwind-merge";

type EventCalendarSheetProps = {
    isoDate: string,
    wrapperClassName?: string,
    dateNumberClassName?: string,
    monthClassName?: string,
    dayClassName?: string,
}

export default function EventCalendarSheet( {
                                                isoDate,
                                                dateNumberClassName,
                                                monthClassName,
                                                wrapperClassName,
                                                dayClassName
                                            }: Readonly<EventCalendarSheetProps> ) {

    const getDayDate = ( dateString: string ) => {
        const date = new Date( dateString );
        return date.toLocaleDateString( "en-US", { day: "2-digit" } );
    }

    const getDayOfWeek = ( dateString: string ) => {
        const date = new Date( dateString );
        return date.toLocaleDateString( "en-US", { weekday: 'long' } );
    }

    const getMonth = ( dateString: string ) => {
        const date = new Date( dateString );
        return date.toLocaleDateString( "en-US", { month: 'long' } );
    }

    return (
        <div
            className={ twMerge( "border bg-muted-foreground dark:bg-foreground text-background max-w-min p-2 rounded-md text-center", wrapperClassName ) }>
            <Text className={ twMerge( "text-sm", dayClassName ) }>
                { getDayOfWeek( isoDate || "" ) }
            </Text>
            <Text styleVariant={ "h4" } className={ dateNumberClassName }>
                { getDayDate( isoDate || "" ) }
            </Text>
            <Text className={ twMerge( "text-sm", monthClassName ) }>
                { getMonth( isoDate || "" ) }
            </Text>
        </div>
    )
}
