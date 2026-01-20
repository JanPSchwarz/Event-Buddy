import PageWrapper from "@/components/shared/PageWrapper.tsx";
import CreateEventForm from "@/components/event/CreateEventForm.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import { PlusCircleIcon } from "lucide-react";

export default function CreateEventPage() {


    return (
        <PageWrapper>
            <MainHeading
                heading={ "Create New Event" }
                subheading={ "" }
                Icon={ PlusCircleIcon }
            />
            <CreateEventForm/>
        </PageWrapper>
    )
}
