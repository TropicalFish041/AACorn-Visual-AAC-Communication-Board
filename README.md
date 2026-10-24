# AACorn - Visual AAC Communication Board

## Team Information

### Team Members
- Harleen Sidhu
- Ruoshui Deng
- Ruohan Tang
- Paul Zhao

---

## Project Description
A customizable visual AAC (Augmentative and Alternative Communication) app for 
autistic children around ages 5–12 who have limited functional speech. 

The main goal is to help a child communicate simple needs and requests using pictograms.
Example: I Want → Water → “I want water.”

For the first version, we are focusing on functional communication instead of 
emotion detection, behaviour prediction, pain assessment, or other therapy-related features.

### Main Features
- make simple requests using pictograms 
- quick access to Help, Stop, and Break 
- search ARASAAC for pictograms 
- add/remove pictograms from the board 
- save the board and communication history


### Domain
Augmentative and Alternative Communication (AAC) for 
Children with Autism Spectrum Disorder (ASD)

### Entities
- Pictogram
  - id
  - label
  - image location


- CommunicationBoard (currently available vocabulary):
  - list of pictograms
  - add pictogram
  - remove pictogram


- CommunicationMessage (what the child actually selected/sent):
  - message text: String
  - selectedPictograms: List<Pictogram>
  - timestamp: LocalDateTime
  - <span style="color: yellow;">***TODO: Professor feedback: The definition of CommunicationMessage is unclear,
    and its relationship to Pictogram, CommunicationBoard and the other entities is not visible.***</span>


- ChildProfile (owns the personalized board and saved message history)
  - customized communication board
  - communication history

Basic relationships:
ChildProfile  ->  CommunicationBoard → Pictograms
ChildProfile  ->   CommunicationMessages

<span style='color: yellow;'> additional entities?</span>

---

## User Stories

#### Child
1. As a child, I want to select pictograms so that I can communicate something I want.
2. As a child, I want quick access to Help, Stop, and Break so that I can communicate important needs easily.

#### Caregiver
1. As a caregiver, I want to search, add, or remove pictograms so that I can customize the child's communication board.
2. As a caregiver, I want to view previous messages so that I can see what the child has communicated.

---

## Use Cases
UC1: Communicate a Request  
Actor: Child
The child chooses I Want.  
> 1. The system displays the available pictograms.  
> 2. The child selects one pictogram.  
> 3. The system creates and displays a message.  
> 4. The message is saved to history.  
> 5. If the child goes back before finishing, no message is saved.

UC2: Search for a Pictogram  
Actor: Caregiver
> 1. The caregiver enters a search term.
> 2. The system searches ARASAAC.
> 3. Matching pictograms are displayed.
> 4. The caregiver can choose a pictogram to add to the board.
> 5. If the API is unavailable or no result is found, the system displays an error message.

UC3: Customize the Board  
Actor: Caregiver
> 1. The caregiver views the current board.
> 2. The caregiver adds or removes a pictogram.
> 3. The board is updated.
> 4. The new board is saved.

UC4: View Communication History  
Actor: Caregiver
> 1. The caregiver opens the communication history.
> 2. The system loads previous messages.
> 3. The messages are displayed in chronological order.

<span style='color: dodgerblue;'>***Extra Feature:***  
UC5: Suggestion for vocabulary  
Actor: Caregiver/Therapist</span>
> 1. The caregiver/therapist reviews vocabulary suggestions from prediction algorithms.
> 2. The caregiver/therapist can update the board by clicking an add button.

<span style='color: dodgerblue;'>***Professor feedback:***  
Prediction needs careful thought. Wrong predictions could negatively affect the children using the app, so it should be an extra feature, not part of the core. If included, it should support the caregiver (who can review and decide), not act directly on the child’s screen.</span>

---

## Minimal Viable Product (MVP)

| Team Member | MVP Use Case |
|-------------|--------------|
| Name | Request communication |
| Name | Help/ Stop/ Delete |
| Name | ARASAAC search |
 | Name | Board customization|
  | Name | History / Persistence |

<span style='color: dodgerblue;'>***Extra Features:***</span>

| Team Member | MVP Use Case |
|-------------|--------------|
| Name | Usage Statistics |
| Name | vocabulary suggestions |

---

## Entities

### Entity 1

### Entity 2

(Add additional entities as needed.)

---

## API Usage

**API Name:**
ARASAAC API

**Documentation:** [Link]
<span style='color: yellow;'>TODO</span>

**How it is used in the program:**
to search for pictograms

---

## Persistence
We plan to use local JSON files to save: the customized board, selected pictograms,
communication history. For development and testing, we will use sample data instead of real clinical data.

---

## Design Diagrams

### UI
![][image1]

[image1]: docs/images/ui.png
### UML Class Diagram 
<span style='color: yellow;'>***TODO:***</span>

### Sequence Diagrams

<span style='color: yellow;'>***TODO:***</span>


---
## How to Run

<span style='color: yellow;'>***TODO:***</span>
[Instructions for running the program.]

---

## Planning
### Phase 1: Framework (Reading Week)

#### 1. UI Design and Navigation
- [ ] Sketch the child and caregiver interfaces.
- [ ] Design the message bar and core buttons (Help, Stop, Delete).
- [ ] Plan navigation between screens.
- [ ] Implement the basic Java Swing GUI using `JFrame` and `JPanel`.

#### 2. ARASAAC API Research
- [ ] Identify the ARASAAC API endpoints for searching pictograms.
- [ ] Test HTTP requests in Java.
- [ ] Retrieve pictogram images and relevant data.
- [ ] Parse API responses.

#### 3. Data Model and Architecture
- [ ] Draft the initial UML class diagram.
- [ ] Define the core entities and their relationships.
- [ ] Design the JSON persistence approach for the communication board and history.
- [ ] Establish the Clean Architecture package structure.

#### 4. Communication Logic
- [ ] Implement pictogram selection and message construction.
- [ ] Handle repeated pictograms and removal of selected pictograms.
- [ ] Implement message deletion and reset functionality.
- [ ] Handle navigation between screens without losing or incorrectly saving selections.
- [ ] Convert selected pictograms into a readable sentence.
- [ ] Investigate text-to-speech functionality.

#### 5. Swing Prototype
- [ ] Display pictograms and selected messages.
- [ ] Connect button events to the communication logic.
- [ ] Implement basic child and caregiver views.
- [ ] Integrate the GUI with the backend.

#### Task Assignments

| Team Member | Task |
|---|---|
| Ruoshui | Swing Prototype |
| Belinda | Data Model and Architecture |
| Paul | ARASAAC API Research |
| Harleen | Communication Logic |

### Phase 2: User Story Backend (Reading Week) 

| Team Member | Task |
|---|---|
| Ruoshui | To be determined |
| Belinda | To be determined |
| Paul | To be determined |
| Harleen | To be determined |

- [ ] UC1: Communicate a Request
- [ ] UC2: Search for a Pictogram
- [ ] UC3: Customize the Board
- [ ] UC4: View Communication History
- [ ] Implement persistence for the communication board and history.
- [ ] Test edge cases and integrate all components.

### Phase 3: Extra Features + Presentation (Post Reading Week)
- [ ] Communication usage statistics (e.g., most frequently selected pictograms).
- [ ] Caregiver-facing vocabulary suggestions based on communication history.
- [ ] Child profile management.
- [ ] Fix bugs, finalize application, prepare for presentation.

---

## Notes

[Any additional project information.]
