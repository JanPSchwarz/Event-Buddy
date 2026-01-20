import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import OrganizationView from "@/components/organization/OrganizationView.tsx";
import PageWrapper from "@/components/shared/PageWrapper.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Alert, AlertDescription } from "@/components/ui/alert.tsx";
import EditOrganizationForm from "@/components/organization/EditOrganizationForm.tsx";
import UpdateOwnerDialog from "@/components/organization/UpdateOwnerDialog.tsx";
import DeleteOrganizationDialog from "@/components/organization/DeleteOrganizationDialog.tsx";
import { NavLink, useSearchParams } from "react-router";
import { ArrowLeftIcon } from "lucide-react";
import { useContextUser } from "@/context/UserProvider.tsx";

type OrganizationOwnerViewProps = {
    orgData: OrganizationResponseDto,
}

export default function OrganizationOwnerView( { orgData }: Readonly<OrganizationOwnerViewProps> ) {

    const [ searchParams, setSearchParams ] = useSearchParams();

    const isEditing = searchParams.get( "edit" ) === "true";

    const { user } = useContextUser();


    const toggleEdit = () => {
        if ( isEditing ) {
            setSearchParams( ( searchParams ) => {
                searchParams.delete( "edit" );
                return searchParams;
            } )
        } else {
            setSearchParams( ( searchParams ) => {
                searchParams.set( "edit", "true" );
                return searchParams;
            } )
        }
    }


    return (
        <PageWrapper className={ "mt-0 md:mt-0" }>
            <Button variant={ "outline" } className={ "mr-auto" } asChild>
                <NavLink to={ `/dashboard/${ user.id }` }>
                    <ArrowLeftIcon/> Dashboard
                </NavLink>
            </Button>
            <div className={ "max-w-[800px] w-full flex-col md:flex-row flex gap-6 justify-between items-center" }>
                <Alert className={ "max-w-max max-h-max" }>
                    <AlertDescription>
                        You are owner of this organization.
                    </AlertDescription>
                </Alert>
                <div
                    className={ "flex gap-4 flex-col items-start w-full md:flex-row md:items-center justify-center" }>
                    <div className={ "flex gap-4" }>
                        <UpdateOwnerDialog disabled={ isEditing } orgaData={ orgData }/>
                        <Button variant={ "outline" } onClick={ toggleEdit }>
                            { isEditing ? "Cancel Editing" : "Edit Organization" }
                        </Button>
                    </div>
                    <DeleteOrganizationDialog organizationId={ orgData.id }
                                              orgaName={ orgData.name }
                                              isEditing={ isEditing }/>
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
