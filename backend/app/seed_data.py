from uuid import UUID, uuid5

from .models import Deck, Difficulty, Prompt, utc_now

NAMESPACE = UUID("2f75a7d8-d66d-4c92-a7cf-50242ce45751")

DECK_SOURCES: list[tuple[str, str, bool, list[str], list[str]]] = [
    ("Hollywood Movies", "Iconic international films", False,
     ["Titanic", "Inception", "Avatar", "Jurassic Park", "The Matrix", "Gladiator", "Frozen", "Jaws", "Rocky", "Toy Story", "The Godfather", "Finding Nemo", "Home Alone", "Interstellar", "The Lion King", "The Avengers", "Cast Away", "La La Land", "Black Panther", "The Dark Knight"],
     ["silent movie", "space adventure", "detective mystery", "superhero finale", "animated friendship", "haunted house", "time travel", "ocean rescue", "wedding comedy", "bank heist"]),
    ("Bollywood Hits", "Hindi cinema favourites across decades", False,
     ["Sholay", "Dangal", "3 Idiots", "Lagaan", "Queen", "Barfi", "Kahaani", "Gully Boy", "Stree", "Andhadhun", "Zindagi Na Milegi Dobara", "Bajrangi Bhaijaan", "Dil Chahta Hai", "Swades", "Krrish", "Don", "Raazi", "Chak De India", "PK", "Drishyam"],
     ["train romance", "family wedding", "college comedy", "village champion", "lost traveller", "secret agent", "dance contest", "cricket drama", "ghost comedy", "musical journey"]),
    ("TV Shows & Anime", "Recognisable television and anime titles", False,
     ["Friends", "Breaking Bad", "Stranger Things", "Squid Game", "Sherlock", "The Office", "Wednesday", "The Crown", "One Piece", "Naruto", "Attack on Titan", "Dragon Ball", "Death Note", "Demon Slayer", "The Simpsons", "Modern Family", "House of the Dragon", "The Mandalorian", "Peaky Blinders", "Arcane"],
     ["hospital drama", "cooking contest", "space crew", "school mystery", "pirate adventure", "family sitcom", "crime documentary", "robot uprising", "talent show", "survival island"]),
    ("Famous Personalities", "Artists, athletes, scientists and leaders", False,
     ["Albert Einstein", "Marie Curie", "Charlie Chaplin", "Michael Jackson", "Taylor Swift", "Cristiano Ronaldo", "Lionel Messi", "Serena Williams", "Usain Bolt", "Amitabh Bachchan", "Shah Rukh Khan", "A R Rahman", "Sachin Tendulkar", "Mary Kom", "Kalpana Chawla", "Frida Kahlo", "Leonardo da Vinci", "William Shakespeare", "Nelson Mandela", "Bruce Lee"],
     ["famous astronaut", "legendary painter", "champion swimmer", "master magician", "world explorer", "comic actor", "rock guitarist", "chess champion", "wildlife presenter", "award-winning chef"]),
    ("Actions & Verbs", "Physical actions made for miming", False,
     ["Milking a cow", "Walking on ice", "Opening a stuck jar", "Chasing a mosquito", "Riding a roller coaster", "Learning to swim", "Flying a kite", "Building a sandcastle", "Juggling oranges", "Sneaking past a guard", "Washing a dog", "Climbing a mountain", "Taking a selfie", "Dancing in the rain", "Escaping quicksand", "Catching a train", "Blowing up balloons", "Wrapping a present", "Planting a tree", "Fixing a bicycle"],
     ["with one hand", "during a storm", "in slow motion", "while half asleep", "on the moon", "without making noise", "at a wedding", "underwater", "in a crowded lift", "while wearing skates"]),
    ("Pop Songs & Bands", "Well-known songs, singers and bands", False,
     ["Thriller", "Single Ladies", "Bohemian Rhapsody", "Baby Shark", "Coldplay", "The Beatles", "ABBA", "Rolling in the Deep", "Uptown Funk", "Shape of You", "Imagine", "Dancing Queen", "We Will Rock You", "Happy", "Bad Guy", "Levitating", "Blinding Lights", "Gangnam Style", "Roar", "Shake It Off"],
     ["air guitar anthem", "rainy day ballad", "stadium chorus", "dance floor hit", "road trip song", "karaoke favourite", "summer melody", "drum solo", "breakup song", "festival band"]),
    ("Brands & Logos", "Familiar products and global brands", False,
     ["Apple", "Nike", "IKEA", "Tesla", "Starbucks", "McDonald's", "Samsung", "LEGO", "Coca-Cola", "Adidas", "Netflix", "Spotify", "Google", "Amazon", "Microsoft", "Ferrari", "Disney", "YouTube", "PlayStation", "WhatsApp"],
     ["sports shoe", "coffee shop", "electric car", "furniture store", "streaming app", "phone maker", "toy company", "fast food", "search engine", "fashion label"]),
    ("Sound Lab", "Prompts made for noises, claps and taps", False,
     ["Helicopter", "Frying bacon", "F1 pit stop", "Monster under the bed", "Thunderstorm", "Popcorn popping", "A squeaky door", "Train leaving a station", "Cat fight", "Robot powering down", "Fireworks", "Dripping tap", "Horse race", "Busy construction site", "Vacuum cleaner", "Mosquito in your ear", "Bowling strike", "Typing on an old keyboard", "Washing machine", "Rocket launch"],
     ["original"]),
    ("Five Second Frenzy", "Name three before the bomb moves on", False,
     ["Name 3 Marvel movies", "Name 3 fast-food chains", "Name 3 blue things", "Name 3 animals with tails", "Name 3 things at a wedding", "Name 3 apps on your phone", "Name 3 pizza toppings", "Name 3 countries in Asia", "Name 3 superheroes", "Name 3 things you can lose", "Name 3 breakfast foods", "Name 3 excuses for being late", "Name 3 things in a kitchen", "Name 3 famous singers", "Name 3 things that fly", "Name 3 board games", "Name 3 things you should not text your boss", "Name 3 ice-cream flavours", "Name 3 sports played with a ball", "Name 3 things found at the beach"],
     ["original"]),
    ("Wild Scenes", "Big visual situations for group and chain games", False,
     ["A DJ fighting an alien", "Baking a cake during an earthquake", "A penguin learning to skateboard", "A magician stuck in a lift", "Pirates ordering coffee", "A superhero afraid of a spider", "A wedding on the moon", "A chef chasing a runaway lobster", "A ghost taking a selfie", "A detective interrogating a parrot", "A robot at a dance class", "A dinosaur working from home", "A mermaid missing the bus", "A vampire at the dentist", "A goalkeeper fighting bees", "A baby driving a taxi", "An astronaut walking a dog", "A ninja assembling furniture", "A king doing karaoke", "A zombie at a job interview"],
     ["original"]),
    ("Secret Words", "Everyday words for subtle clues and suspicious answers", False,
     ["Airport", "Banana", "Beach", "Bicycle", "Birthday", "Camera", "Chocolate", "Cinema", "Coffee", "Concert", "Dentist", "Desert", "Dragon", "Elevator", "Festival", "Firefighter", "Football", "Forest", "Hospital", "Hotel", "Internet", "Island", "Library", "Market", "Mirror", "Museum", "Ocean", "Pancake", "Passport", "Penguin", "Pizza", "Police", "Rain", "Restaurant", "Robot", "School", "Snow", "Space", "Supermarket", "Train"],
     ["original"]),
    ("Crowd Questions", "Subjective questions with no single right answer", False,
     ["Best late-night snack?", "Worst place to fall asleep?", "Most suspicious thing to carry into a bank?", "A movie everyone pretends to understand?", "Best animal to have as a tiny sidekick?", "Worst song for a wedding entrance?", "Something people always lose?", "Best excuse for cancelling plans?", "Most awkward thing to say in a lift?", "A food that should never be blue?", "Best superpower for a lazy person?", "Worst thing to hear from a pilot?", "A job a ghost would be good at?", "Most overrated household chore?", "Best thing to find in an old jacket?", "A terrible name for a restaurant?", "Most dramatic way to enter a room?", "A word that sounds funny when repeated?", "Best object to survive a zombie attack?", "Worst gift for a first date?"],
     ["original"]),
    ("Signal Lines", "Read the room on strange social spectrums", False,
     ["Harmless prank | Friendship-ending prank", "Tiny inconvenience | Day ruined", "Awkward silence | Perfect silence", "Budget snack | Luxury meal", "Background music | Dance-floor emergency", "Mildly suspicious | Definitely an alien", "Useful talent | Useless party trick", "Quiet weekend | Legendary weekend", "Bad excuse | Somehow believable", "Friendly competition | Lifelong rivalry", "Messy room | Archaeological site", "Small favour | Heroic sacrifice", "Normal pet | Final boss pet", "Slightly spicy | Call the fire brigade", "Casual outfit | Red-carpet event", "Gentle reminder | Personal attack", "Minor spoiler | Movie completely ruined", "Simple hobby | Entire personality", "Short queue | Civilization has collapsed", "Confident guess | Undeniable fact"],
     ["original"]),
    ("Truth or Dare", "Family-friendly truths and playful challenges", False,
     ["What is your funniest childhood memory?|Do your best robot dance", "What food could you eat every day?|Balance a cushion on your head for 20 seconds", "What is your most useless talent?|Speak like a movie villain until your next turn", "What was your worst fashion phase?|Act like a cat choosing its owner", "What always makes you laugh?|Sing the chorus of any song dramatically", "What is the strangest dream you remember?|Walk across the room in slow motion", "Who was your first celebrity crush?|Pretend to accept an imaginary award", "What app do you waste the most time on?|Do ten seconds of invisible jump rope", "What is a harmless lie you often tell?|Let the group give you a new nickname", "What is the weirdest snack combination you enjoy?|Imitate another player until someone guesses"],
     ["original"]),
    ("Truth or Dare After Dark", "Adults-only truths and bolder dares", True,
     ["What is your worst date story?|Send a harmless flirty emoji to the fifth recent contact", "What is your biggest dating red flag?|Deliver your best terrible pickup line", "Have you ever texted an ex after midnight?|Let the group rewrite your dating bio", "What is the boldest thing you did on a date?|Slow dance with an imaginary partner", "Who here would survive a chaotic holiday with you?|Re-enact your most awkward first-date greeting", "What is your most irrational jealousy?|Read your last sent message dramatically", "What is a secret deal-breaker?|Let another player choose your profile-photo pose", "What compliment do you secretly love?|Flirt with a household object for fifteen seconds", "What is your funniest walk-of-shame story?|Do your best seductive weather forecast", "What is the strangest place you fell asleep?|Call someone gorgeous using only movie quotes"],
     ["original"]),
    ("Most Likely", "Vote for who best fits the chaos", False,
     ["Most likely to get arrested on vacation", "Most likely to become a millionaire", "Most likely to survive a zombie apocalypse", "Most likely to forget their own birthday", "Most likely to become internet famous", "Most likely to adopt ten pets", "Most likely to start a conspiracy theory", "Most likely to accidentally join a cult", "Most likely to cry during a cartoon", "Most likely to move to another country tomorrow"],
     ["original"]),
    ("Never Have I Ever", "Confessions with five-heart survival", False,
     ["Never have I ever lied about my age", "Never have I ever pretended to know a song", "Never have I ever sent a message to the wrong person", "Never have I ever fallen asleep in public", "Never have I ever eaten food off the floor", "Never have I ever blamed someone else for my mistake", "Never have I ever laughed during a serious moment", "Never have I ever forgotten someone's name immediately", "Never have I ever snooped through a friend's fridge", "Never have I ever faked being sick"],
     ["original"]),
    ("Fake It Trivia", "Obscure facts ready for convincing fake answers", False,
     ["What is the national animal of Scotland?|Unicorn", "What fruit floats because a quarter of it is air?|Apple", "What is a group of flamingos called?|Flamboyance", "Which planet has the shortest day?|Jupiter", "What is the dot over a lowercase i called?|Tittle", "Which country invented paper?|China", "What is the largest organ in the human body?|Skin", "Which animal has fingerprints almost identical to humans?|Koala", "What was the first toy advertised on television?|Mr Potato Head", "Which sea creature has three hearts?|Octopus"],
     ["original"]),
    ("Draw & Guess", "Simple words designed for finger drawing", False,
     ["Volcano", "Telescope", "Kangaroo", "Birthday cake", "Roller coaster", "Treasure map", "Snowman", "Hot-air balloon", "Lighthouse", "Octopus"],
     ["original"]),
    ("Would You Rather", "Impossible choices for instant debate", False,
     ["Lose the ability to read|Lose the ability to speak", "Always be ten minutes late|Always be twenty minutes early", "Explore outer space|Explore the deepest ocean", "Have a rewind button|Have a pause button", "Only whisper|Only shout", "Fight one horse-sized duck|Fight one hundred duck-sized horses", "Know every language|Play every instrument", "Give up music|Give up movies", "Live without internet|Live without air conditioning", "Be able to fly|Be invisible"],
     ["original"]),
    ("Twin Words", "Related word pairs for hidden-spy deduction", False,
     ["Coffee|Tea", "Beach|Desert", "Cat|Tiger", "Train|Bus", "Pizza|Burger", "Moon|Sun", "Rain|Snow", "Doctor|Nurse", "Laptop|Tablet", "River|Ocean"],
     ["original"]),
    ("After Dark", "Adults-only party situations", True,
     ["Walk of shame", "Beer pong", "Bad hangover", "Awkward first date", "Secret crush", "Karaoke after midnight", "Losing your room key", "Dancing on a table", "Truth or dare", "Texting an ex", "Blind date", "Party gatecrasher", "Flirting badly", "Morning after", "Spilling a cocktail", "Hiding from the neighbours", "Terrible pickup line", "Midnight snack", "Fake phone number", "Last call"],
     ["at a reunion", "on holiday", "during a house party", "with your best friend", "at midnight", "after two drinks", "in a hotel lobby", "on the dance floor", "at brunch", "during a video call"]),
]


def built_in_decks() -> list[Deck]:
    decks: list[Deck] = []
    now = utc_now()
    for rank, (title, description, restricted, base, modifiers) in enumerate(DECK_SOURCES, start=1):
        deck_id = uuid5(NAMESPACE, title)
        cards: list[Prompt] = []
        for base_index, phrase in enumerate(base):
            for modifier_index, modifier in enumerate(modifiers):
                text = phrase if modifier_index == 0 else f"{phrase}: {modifier}"
                cards.append(Prompt(
                    id=uuid5(deck_id, text),
                    text=text,
                    difficulty=[Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD][(base_index + modifier_index) % 3],
                    hint=description,
                ))
        decks.append(Deck(
            id=deck_id,
            title=title,
            description=description,
            version=1,
            updated_at=now,
            card_count=len(cards),
            age_restricted=restricted,
            featured_rank=rank if rank <= 4 else None,
            cards=cards,
        ))
    return decks

