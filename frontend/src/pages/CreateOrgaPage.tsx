import PageWrapper from "@/components/shared/PageWrapper.tsx";
import CreateOrganizationForm from "@/components/organization/CreateOrganizationForm.tsx";
import { CirclePlus } from 'lucide-react';
import MainHeading from "@/components/shared/MainHeading.tsx";


export default function CreateOrgaPage() {

    return (
        <PageWrapper>
            <MainHeading
                heading={ "Create New Organization" }
                subheading={ "" }
                Icon={ CirclePlus }
            />
            <CreateOrganizationForm/>
        </PageWrapper>

    )
}
