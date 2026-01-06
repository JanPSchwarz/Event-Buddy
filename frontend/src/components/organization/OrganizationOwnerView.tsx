import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import OrganizationView from "@/components/organization/OrganizationView.tsx";
import PageWrapper from "@/components/PageWrapper.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Alert, AlertDescription } from "@/components/ui/alert.tsx";
import { useState } from "react";
import EditOrganizationForm from "@/components/organization/EditOrganizationForm.tsx";

type OrganizationOwnerViewProps = {
    orgData: OrganizationResponseDto,
}

export default function OrganizationOwnerView( { orgData }: Readonly<OrganizationOwnerViewProps> ) {

    const [ isEditing, setIsEditing ] = useState( false );

    const toggleEdit = () => {
        setIsEditing( !isEditing );
    }

    return (
        <PageWrapper>
            <div className={ "max-w-[800px] w-full flex gap-6 justify-between" }>
                <Alert className={ "max-w-max" }>
                    <AlertDescription>
                        You are owner of this organization.
                    </AlertDescription>
                </Alert>
                <Button variant={ "outline" } onClick={ toggleEdit }>
                    { isEditing ? "Cancel Editing" : "Edit Organization" }
                </Button>
            </div>
            { isEditing ?
                <EditOrganizationForm organizationData={ orgData } closeEdit={ toggleEdit }/>
                :
                <OrganizationView orgaData={ orgData }/>
            }
        </PageWrapper>
    )
}
