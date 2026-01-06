import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import OrganizationForm from "@/components/organization/OrganizationForm.tsx";
import { useCreateOrganization } from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import type { OrganizationRequestDto } from "@/api/generated/openAPIDefinition.schemas.ts";

export default function CreateOrganizationForm() {
    const createOrga = useCreateOrganization( {
        axios: {
            withCredentials: true,
        }
    } );

    const handleSubmit = ( organizationDto: OrganizationRequestDto, imageFile: File | null ) => {
        createOrga.mutate(
            {
                data:
                    {
                        organization: organizationDto,
                        image: imageFile || undefined
                    }
            },
            {
                onSuccess: () => {
                    toast.success( "Organization created successfully!" );
                },
                onError: ( error ) => {
                    console.error( "Error creating organization:", error );
                    toast.error( error.response?.data.error || "Error creating orga" );
                }
            }
        );
    }

    return (
        <Card className={ "md:p-4 w-full max-w-[900px] mx-auto" }>
            <CardHeader>
                <CardTitle>
                    Create Organization
                </CardTitle>
                <CardDescription>
                    Create your Organization here.
                </CardDescription>
            </CardHeader>
            <CardContent>
                <OrganizationForm onSubmit={ handleSubmit }/>
            </CardContent>

        </Card>
    )
}
