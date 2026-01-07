import PageWrapper from "@/components/PageWrapper.tsx";
import CreateOrganizationForm from "@/components/organization/CreateOrganizationForm.tsx";
import Text from "@/components/typography/Text.tsx";
import { CirclePlus } from 'lucide-react';


export default function CreateOrgaPage() {

    return (
        <PageWrapper>
            <div className={ "w-full flex justify-between border-b border-primary" }>
                <Text asTag={ "h1" } styleVariant={ "h1" }>
                    New Organization
                    <CirclePlus className={ "inline -translate-y-1/2" }/>
                </Text>

                <Text asTag={ "span" } styleVariant={ "smallMuted" } className={ "mt-auto" }>
                    Create your organization
                </Text>
            </div>
            <CreateOrganizationForm/>
        </PageWrapper>

    )
}
