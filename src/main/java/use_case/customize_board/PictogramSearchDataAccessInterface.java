package use_case.customize_board;

import entity.Pictogram;

import java.util.List;

public interface PictogramSearchDataAccessInterface {

    /**
     * Searches ARASAAC for pictograms matching the keyword.
     * Returns an empty list if nothing matches.
     *
     * @throws PictogramSearchException if the API cannot be reached
     */
    List<Pictogram> searchPictograms(String keyword) throws PictogramSearchException;
}
