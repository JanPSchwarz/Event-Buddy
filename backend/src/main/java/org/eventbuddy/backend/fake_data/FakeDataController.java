package org.eventbuddy.backend.fake_data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fake-data")
@Tag(name = "Fake data", description = "creates and deletes fake data for development purposes")
@Profile("dev")
public class FakeDataController {

    FakeDataService fakeDataService;

    public FakeDataController( FakeDataService fakeDataService ) {
        this.fakeDataService = fakeDataService;
    }

    @PostMapping("/create-fake-data")
    @Operation(
            summary = "Create fake data",
            description = "Creates custom number fake data for testing purposes."
    )
    public void createFakeData() {
        fakeDataService.createFakeData( 20 );
    }

    @DeleteMapping("/delete-all-data")
    @Operation(
            summary = "Delete all fake data",
            description = "Deletes all data from the system."
    )
    public void deleteAllFakeData() {
        fakeDataService.deleteAllFakeData();
    }
}
