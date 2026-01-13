import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import OrganizationView from "@/components/organization/OrganizationView.tsx";
import PageWrapper from "@/components/PageWrapper.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Alert, AlertDescription } from "@/components/ui/alert.tsx";
import { useState } from "react";
import EditOrganizationForm from "@/components/organization/EditOrganizationForm.tsx";
import UpdateOwnerDialog from "@/components/organization/UpdateOwnerDialog.tsx";
import { useDeleteOrganization } from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import { useNavigate } from "react-router";
import { useQueryClient } from "@tanstack/react-query";

type OrganizationOwnerViewProps = {
    orgData: OrganizationResponseDto,
}

export default function OrganizationOwnerView( { orgData }: Readonly<OrganizationOwnerViewProps> ) {

    const [ isEditing, setIsEditing ] = useState( false );

    const navigate = useNavigate();

    const queryClient = useQueryClient();

    const deleteOrga = useDeleteOrganization( {
        axios: { withCredentials: true }
    } );

    const toggleEdit = () => {
        setIsEditing( !isEditing );
    }

    const handleDeleteOrganization = () => {
        if ( isEditing ) return;


        deleteOrga.mutate(
            { organizationId: orgData.id },
            {
                onSuccess: () => {
                    toast.success( "Organization deleted successfully." );
                    queryClient.invalidateQueries();
                    navigate( "/" );

                },
                onError: ( error ) => {
                    console.error( "Error deleting organization:", error );
                }
            }
        )
    }

    return (
        <PageWrapper>
            <div className={ "max-w-[800px] w-full flex gap-6 justify-between items-center" }>
                <Alert className={ "max-w-max max-h-max" }>
                    <AlertDescription>
                        You are owner of this organization.
                    </AlertDescription>
                </Alert>
                <div className={ "flex gap-2 flex-col items-end md:flex-row md:items-center justify-center" }>
                    <UpdateOwnerDialog disabled={ isEditing } orgaData={ orgData }/>
                    <Button variant={ "outline" } onClick={ toggleEdit }>
                        { isEditing ? "Cancel Editing" : "Edit Organization" }
                    </Button>
                    <Button variant={ "destructive" } disabled={ isEditing } onClick={ handleDeleteOrganization }>
                        Delete Organization
                    </Button>
                </div>
            </div>
            { isEditing ?
                <EditOrganizationForm organizationData={ orgData } closeEdit={ toggleEdit }/>
                :
                <OrganizationView orgaData={ orgData }/>
            }
        </PageWrapper>
    )
}
