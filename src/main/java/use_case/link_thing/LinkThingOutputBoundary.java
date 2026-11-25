package use_case.link_thing;

/**
 * Implemented by the presenter.
 */
public interface LinkThingOutputBoundary {

    void prepareSuccessView(LinkThingOutputData outputData);

    void prepareFailView(String errorMessage);
}