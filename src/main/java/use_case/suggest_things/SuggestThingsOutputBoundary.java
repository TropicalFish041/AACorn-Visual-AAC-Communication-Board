package use_case.suggest_things;

/**
 * Implemented by the presenter.
 */
public interface SuggestThingsOutputBoundary {

    void prepareSuccessView(SuggestThingsOutputData outputData);

    void prepareFailView(String errorMessage);
}
