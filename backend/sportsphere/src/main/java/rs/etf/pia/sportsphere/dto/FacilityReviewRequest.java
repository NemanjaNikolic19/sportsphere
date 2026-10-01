package rs.etf.pia.sportsphere.dto;

public class FacilityReviewRequest {

    private String reaction;
    private String commentText;

    public FacilityReviewRequest() {
    }

    public FacilityReviewRequest(
            String reaction,
            String commentText) {

        this.reaction = reaction;
        this.commentText = commentText;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}