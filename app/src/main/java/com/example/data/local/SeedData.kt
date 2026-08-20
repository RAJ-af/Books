package com.example.data.local

import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.ReadingProgress

data class SeedBookWithChapters(
    val book: Book,
    val chapters: List<Chapter>,
    val initialProgress: ReadingProgress? = null
)

object SeedData {

    fun getSeedBooks(): List<SeedBookWithChapters> {
        val books = mutableListOf<SeedBookWithChapters>()

        // ==========================================
        // 1. DESIGN CATEGORY
        // ==========================================
        val bauhausBook = Book(
            id = 1L,
            title = "Bauhaus: 1919–1933",
            author = "Magdalena Droste",
            coverImageUri = "",
            rating = 4.8f,
            pageCount = 400,
            genre = "Design",
            description = "A definitive study of the revolutionary German art school that transformed modern architecture, typography, industrial craft, and 20th-century visual culture.",
            isImportedPdf = false,
            colorHex = "#285698",
            accentTint = "#E05A47"
        )
        val bauhausChapters = listOf(
            Chapter(
                id = 101L,
                bookId = 1L,
                number = 1,
                title = "The Weimar Experiment",
                subtitle = "Art, Craft, and the New Architecture",
                estimatedReadMinutes = 9,
                content = """
                    The Bauhaus was founded in 1919 in the city of Weimar by German architect Walter Gropius. Its core proclamation was audacious: to bridge the chasm between fine art and functional craft, creating a unified work of art—the Gesamtkunstwerk—in which all arts, from architecture to graphic design, would ultimately coalesce.

                    "Architects, sculptors, painters—we all must return to craftsmanship! For there is no such thing as 'art by profession.' There is no essential difference between the artist and the artisan."
                    — Walter Gropius, Bauhaus Manifesto (1919)

                    In the post-World War I era, Weimar Germany was rebuilding its spiritual and physical identity. The school embraced fundamental color theory, elemental geometric forms, and a relentless focus on clean materials: unadorned tubular steel, polished glass, exposed concrete, and raw textile weaves. Johannes Itten’s legendary preliminary course forced incoming students to unlearn conventional academic painting and rediscover rhythm, contrast, and texture from first principles.
                """.trimIndent()
            ),
            Chapter(
                id = 102L,
                bookId = 1L,
                number = 2,
                title = "Form Follows Function",
                subtitle = "The Dessau Years and Industrial Synthesis",
                estimatedReadMinutes = 11,
                content = """
                    When the school relocated to Dessau in 1925, Gropius designed a building that became an instant landmark of modernist architecture. Glass curtain walls hung weightlessly from reinforced concrete frames, letting daylight illuminate the workshops from sunrise to dusk.

                    Here, the workshop masters—László Moholy-Nagy, Marcel Breuer, Wassily Kandinsky, and Paul Klee—began producing prototypes specifically engineered for mass manufacture. Breuer’s famous Wassily Chair replaced heavy, upholstered Victorian armchairs with curved chrome bicycle tubes and tensioned leather straps. It was light enough to move with one hand, yet virtually indestructible.

                    Typography also underwent a radical transformation under Herbert Bayer. Bayer discarded capital letters entirely, arguing that spoken language does not differentiate between uppercase and lowercase letters, and designed universal sans-serif letterforms that prioritized speed, clarity, and mechanical printing efficiency.
                """.trimIndent()
            ),
            Chapter(
                id = 103L,
                bookId = 1L,
                number = 3,
                title = "The Grammar of Color and Space",
                subtitle = "Klee, Kandinsky, and Spatial Harmony",
                estimatedReadMinutes = 12,
                content = """
                    Paul Klee arrived at the Bauhaus with an analytical approach to line and movement. To Klee, a drawing was not a static representation, but 'taking a line for a walk.' He lectured on active, medial, and passive lines, demonstrating how visual tension mimics natural growth in organic biology.

                    Kandinsky complemented Klee with his rigorous breakdown of color and emotional temperature. A triangle was fundamentally yellow—sharp, active, and vibrating with centrifugal energy. A circle was deep blue—peaceful, self-contained, and meditative. A square was grounded red—stable, solid, and structural.

                    Together, these theoretical foundations gave birth to an entire generation of designers who understood that beauty in everyday objects is not applied decoration, but the natural resonance of thoughtful geometry and functional honesty.
                """.trimIndent()
            ),
            Chapter(
                id = 104L,
                bookId = 1L,
                number = 4,
                title = "The Global Diaspora",
                subtitle = "From Berlin to Chicago, Black Mountain, and Beyond",
                estimatedReadMinutes = 8,
                content = """
                    Under political pressure, the Bauhaus closed its doors in Berlin in 1933. However, its dispersal across the globe ensured its immortality. Mies van der Rohe and László Moholy-Nagy went to Chicago, founding the New Bauhaus and reshaping the American skyline with steel-and-glass skyscrapers.

                    Josef and Anni Albers took their expertise to Black Mountain College in North Carolina, nurturing students who would define post-war modernism. Today, whether we look at the typography on an iPhone screen, the clean lines of Scandinavian furniture, or modular architectural systems, the spirit of the Bauhaus remains the foundational language of modern design.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(bauhausBook, bauhausChapters, ReadingProgress(1L, 101L, 25f)))

        val dieterRamsBook = Book(
            id = 2L,
            title = "Dieter Rams: The Complete Works",
            author = "Klaus Klemp",
            coverImageUri = "",
            rating = 4.9f,
            pageCount = 344,
            genre = "Design",
            description = "The definitive retrospective of Dieter Rams' legendary 40-year career at Braun and Vitsœ, exploring the Ten Principles for Good Design that shaped modern consumer technology.",
            isImportedPdf = false,
            colorHex = "#E05A47",
            accentTint = "#C0392B"
        )
        val dieterRamsChapters = listOf(
            Chapter(
                id = 201L,
                bookId = 2L,
                number = 1,
                title = "Weniger, aber besser",
                subtitle = "Less, but better — The Braun Philosophy",
                estimatedReadMinutes = 10,
                content = """
                    In the late 1950s, when consumer electronics were disguised as opulent baroque wooden furniture with gilded dials, Dieter Rams introduced a radical aesthetic at Braun. His audio systems, shavers, and calculators were stark, serene, and unapologetically functional.

                    "Indifference towards people and the reality in which they live is actually the one and only cardinal sin in design."
                    — Dieter Rams

                    The SK4 phonograph—affectionately dubbed 'Snow White’s Coffin' due to its transparent acrylic cover and clean white metal chassis—revolutionized home audio. Rams believed that appliances should not scream for attention. They should recede into the background like a well-mannered English butler, performing their duty flawlessly without dominating the room.
                """.trimIndent()
            ),
            Chapter(
                id = 202L,
                bookId = 2L,
                number = 2,
                title = "The Ten Principles for Good Design",
                subtitle = "A Timeless Compass for Creators",
                estimatedReadMinutes = 14,
                content = """
                    As consumer culture became increasingly wasteful in the 1970s, Rams asked himself: 'Is my design good design?' To answer this, he codified his famous Ten Principles:

                    1. Good design is innovative.
                    2. Good design makes a product useful.
                    3. Good design is aesthetic.
                    4. Good design makes a product understandable.
                    5. Good design is unobtrusive.
                    6. Good design is honest.
                    7. Good design is long-lasting.
                    8. Good design is thorough down to the last detail.
                    9. Good design is environmentally friendly.
                    10. Good design is as little design as possible.

                    Each principle was not a restrictive dogma, but a moral commitment to clarity, longevity, and respect for the user's mental bandwidth.
                """.trimIndent()
            ),
            Chapter(
                id = 203L,
                bookId = 2L,
                number = 3,
                title = "System 606 and Modular Living",
                subtitle = "The Enduring Legacy of Vitsœ",
                estimatedReadMinutes = 9,
                content = """
                    Alongside his work at Braun, Rams co-founded the 606 Universal Shelving System for Vitsœ in 1960. Designed to grow, adapt, and move with its owner across generations, the 606 system is still produced with exact interchangeable tolerances today.

                    A customer who bought a shelf bracket in 1965 can buy an extension component today, and it will click into place effortlessly. This circular, modular ethos challenged the prevailing culture of planned obsolescence decades before sustainability became a mainstream industry term.
                """.trimIndent()
            ),
            Chapter(
                id = 204L,
                bookId = 2L,
                number = 4,
                title = "The Digital Descendants",
                subtitle = "How Rams Influenced Silicon Valley",
                estimatedReadMinutes = 8,
                content = """
                    Sir Jony Ive, former Chief Design Officer at Apple, has frequently acknowledged Rams as one of his greatest inspirations. The physical similarity between the Braun T3 pocket radio and the original iPod, or the Braun ET66 calculator and the iOS Calculator app, is an affectionate homage to Rams' enduring visual clarity.

                    Rams himself smiled when asked about Apple's designs, noting that imitation is the sincerest form of flattery—and more importantly, proof that the principles of simplicity, utility, and understated elegance remain universally true across both analog and digital eras.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(dieterRamsBook, dieterRamsChapters, ReadingProgress(2L, 201L, 45f)))

        val donNormanBook = Book(
            id = 3L,
            title = "The Design of Everyday Things",
            author = "Don Norman",
            coverImageUri = "",
            rating = 4.7f,
            pageCount = 368,
            genre = "Design",
            description = "The ultimate guide to human-centered design, affordances, signifiers, and why smart people struggle with seemingly simple everyday objects.",
            isImportedPdf = false,
            colorHex = "#E6A728",
            accentTint = "#B45309"
        )
        val donNormanChapters = listOf(
            Chapter(
                id = 301L,
                bookId = 3L,
                number = 1,
                title = "The Psychopathology of Everyday Things",
                subtitle = "Affordances, Signifiers, and the Norman Door",
                estimatedReadMinutes = 13,
                content = """
                    Signifiers are the most important addition to the chapter, a concept first introduced in my book Living with Complexity. The first edition had a focus upon affordances, but although affordances make sense for interaction with physical objects, they are confusing when dealing with virtual ones.

                    As a result, affordances have created much confusion in the world of design. Affordances define what actions are possible. Signifiers specify how people discover those possibilities: signifiers are signs, perceptible signals of what can be done. Signifiers are of far more importance to designers than are affordances.

                    Have you ever walked up to a door, pushed it with full confidence, only to bounce off it because it required a pull? You felt foolish. But you are not to blame: the designer who put a flat brass handle on a pull-door committed a cardinal sin. A flat plate signals PUSH; a rounded handle signals PULL. When the physical affordance contradicts the required operation, failure is inevitable.
                """.trimIndent()
            ),
            Chapter(
                id = 302L,
                bookId = 3L,
                number = 2,
                title = "The Psychology of Everyday Actions",
                subtitle = "The Seven Stages of Action and Gulfs of Execution",
                estimatedReadMinutes = 11,
                content = """
                    When people interact with any system—whether setting a modern digital thermostat or navigating a complex smartphone settings menu—they navigate two fundamental gaps:

                    1. The Gulf of Execution: "How do I do what I want to do?"
                    2. The Gulf of Evaluation: "Did the system do what I intended, and what state is it in now?"

                    To bridge these gulfs, designers must provide immediate, unambiguous feedback. When you tap a button on a touchscreen, a subtle haptic pulse or subtle color change reassures the nervous system that your intent was recognized. Without feedback, users double-tap in frustration, generating errors and anxiety.
                """.trimIndent()
            ),
            Chapter(
                id = 303L,
                bookId = 3L,
                number = 3,
                title = "Knowledge in the Head and in the World",
                subtitle = "Why We Don't Need to Remember Everything",
                estimatedReadMinutes = 10,
                content = """
                    Humans possess remarkably limited working memory—usually around four chunks of information at any given time. Yet, we navigate complex daily routines smoothly because most necessary information is stored outside our brains: in the world itself.

                    Natural mappings take advantage of physical analogies and cultural conventions. Consider stove burner controls: if the four burners are laid out in a rectangular grid, the four control knobs should be laid out in the exact same rectangular arrangement. When designers line up the knobs in a straight horizontal row, users are forced to memorize which knob controls which burner—turning simple cooking into cognitive labor.
                """.trimIndent()
            ),
            Chapter(
                id = 304L,
                bookId = 3L,
                number = 4,
                title = "Design Thinking in Practice",
                subtitle = "Double Diamond and Solving the Real Problem",
                estimatedReadMinutes = 12,
                content = """
                    Engineers and managers frequently rush to solve the symptom rather than the underlying root cause. The Double Diamond design process addresses this with four iterative phases: Discover, Define, Develop, and Deliver.

                    Never solve the problem you were given immediately; first, investigate whether it is the true problem. If a client asks for a faster elevator because passengers complain about waiting times, installing mirrors in the lobby often resolves the complaints completely. The passengers weren't distressed by the mathematical duration—they were distressed by the boredom of idle waiting.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(donNormanBook, donNormanChapters, ReadingProgress(3L, 301L, 10f)))

        val solidProductBook = Book(
            id = 4L,
            title = "Solid Product Design Exercises",
            author = "Wiley Design",
            coverImageUri = "",
            rating = 4.6f,
            pageCount = 280,
            genre = "Design",
            description = "A practical workshop handbook with 100 hands-on design exercises covering industrial ergonomics, tactile prototyping, and design execution.",
            isImportedPdf = false,
            colorHex = "#C0392B",
            accentTint = "#962D22"
        )
        val solidProductChapters = listOf(
            Chapter(
                id = 401L,
                bookId = 4L,
                number = 1,
                title = "Rapid Prototyping & Foam Modeling",
                subtitle = "Thinking Through Your Hands",
                estimatedReadMinutes = 8,
                content = """
                    CAD software is indispensable, but it creates a false sense of scale and tactile weight. Blue modeling foam and hot-wire cutters allow an industrial designer to carve, refine, and physically grasp a handheld tool within twenty minutes.

                    When testing a new grip, pay attention to the palm arch, thumb stabilization shelf, and wrist pronation. A contour that looks sleek on a high-resolution 3D monitor often causes wrist fatigue within five minutes of continuous usage.
                """.trimIndent()
            ),
            Chapter(
                id = 402L,
                bookId = 4L,
                number = 2,
                title = "Color, Material, and Finish (CMF)",
                subtitle = "Sensory Touchpoints and Durability",
                estimatedReadMinutes = 9,
                content = """
                    CMF is not merely decorative styling—it communicates value, tactile feedback, and thermal expectation. Bead-blasted anodized aluminum feels cool and precision-engineered; soft-touch matte elastomers provide confident non-slip grip in wet environments.

                    Consider how materials age under UV light, skin oils, and repeated friction. A well-designed product develops a dignified patina over years of use rather than peeling or degrading into sticky decay.
                """.trimIndent()
            ),
            Chapter(
                id = 403L,
                bookId = 4L,
                number = 3,
                title = "Designing for Disassembly",
                subtitle = "Circular Engineering Principles",
                estimatedReadMinutes = 10,
                content = """
                    Sustainable product design begins on the drafting board by eliminating permanent adhesives wherever snap-fits or standard Torx fasteners can be used. If a battery or switch cannot be replaced in under five minutes using ordinary tools, the product is destined for a landfill prematurely.

                    Design each component for straightforward separation into single-stream recyclable materials: aluminum, polycarbonate, and copper.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(solidProductBook, solidProductChapters, ReadingProgress(4L, 401L, 0f)))

        // ==========================================
        // 2. PSYCHOLOGY CATEGORY
        // ==========================================
        val readPeopleBook = Book(
            id = 5L,
            title = "Read People Like a Book",
            author = "Patrick King",
            coverImageUri = "",
            rating = 4.6f,
            pageCount = 240,
            genre = "Psychology",
            description = "How to analyze, understand, and predict people's emotions, thoughts, intentions, and behaviors through behavioral cues and micro-expressions.",
            isImportedPdf = false,
            colorHex = "#2B4C5F",
            accentTint = "#1F3543"
        )
        val readPeopleChapters = listOf(
            Chapter(
                id = 501L,
                bookId = 5L,
                number = 1,
                title = "Establishing the Baseline",
                subtitle = "The Foundation of Behavioral Observation",
                estimatedReadMinutes = 10,
                content = """
                    Before you can detect deception, nervousness, or sudden attraction in another person, you must first know how they act when they are completely relaxed and under no psychological pressure. This is known as the baseline.

                    How fast do they naturally speak? Where do their hands rest when chatting casually? How frequently do they blink?

                    The biggest mistake amateur profilers make is assuming that crossing one's arms automatically signifies defensiveness. If a room is cold, or if an individual naturally finds arm-crossing comfortable, that posture is simply part of their baseline. Deviation from the baseline—especially sudden clustering of non-verbal cues—is where true psychological insight lies.
                """.trimIndent()
            ),
            Chapter(
                id = 502L,
                bookId = 5L,
                number = 2,
                title = "Micro-Expressions & Facial Clusters",
                subtitle = "The Involuntary Truth of the Limbic System",
                estimatedReadMinutes = 12,
                content = """
                    Dr. Paul Ekman’s research demonstrated that human facial expressions of primary emotions—joy, sadness, anger, fear, surprise, disgust, and contempt—are biologically universal across all cultures.

                    Micro-expressions flash across a person's face in as little as 1/25th of a second before the prefrontal cortex can consciously mask them. A momentary tightening of the lip corners (contempt), a brief flare of the nostrils accompanied by lowered brows (anger), or a fleeting micro-smile during inappropriate moments (duping delight) reveal raw subconscious reactions.
                """.trimIndent()
            ),
            Chapter(
                id = 503L,
                bookId = 5L,
                number = 3,
                title = "The Feet Never Lie",
                subtitle = "Lower Body Language and Directional Intent",
                estimatedReadMinutes = 8,
                content = """
                    When humans learned to deceive, we began controlling the upper body first: managing our eyes, voice pitch, and facial smile. However, evolutionary psychology shows that our feet and legs remain the most honest indicators of subconscious desire.

                    If you are speaking to someone whose torso is turned toward you, but whose feet are pointed toward the exit, their mind has already departed the conversation. Conversely, if someone's feet mirror your stance with open ankles, psychological rapport is genuine and deep.
                """.trimIndent()
            ),
            Chapter(
                id = 504L,
                bookId = 5L,
                number = 4,
                title = "Cognitive Filters and Motivations",
                subtitle = "Understanding Values, Biases, and Drives",
                estimatedReadMinutes = 11,
                content = """
                    Every human being interprets reality through internal filters shaped by early childhood experiences, core fears, and subconscious desires. The Enneagram and Big Five personality frameworks help identify whether an individual is primarily driven by a need for safety, status, connection, or autonomy.

                    When you understand a person's primary emotional currency, you no longer judge their behavior as irrational—you understand the internal calculus that makes it completely logical to them.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(readPeopleBook, readPeopleChapters, ReadingProgress(5L, 501L, 60f)))

        val subconsciousBook = Book(
            id = 6L,
            title = "The Power of Your Subconscious Mind",
            author = "Joseph Murphy",
            coverImageUri = "",
            rating = 4.7f,
            pageCount = 304,
            genre = "Psychology",
            description = "A timeless spiritual and psychological classic unlocking the incredible healing, transformative, and manifesting power stored in the subconscious mind.",
            isImportedPdf = false,
            colorHex = "#4A4E69",
            accentTint = "#22223B"
        )
        val subconsciousChapters = listOf(
            Chapter(
                id = 601L,
                bookId = 6L,
                number = 1,
                title = "The Treasure House Within You",
                subtitle = "The Infinite Potential of Inner Consciousness",
                estimatedReadMinutes = 9,
                content = """
                    Infinite riches are all around you if you will open your mental eyes and behold the treasure house of infinity within you. There is a gold mine inside you from which you can extract everything you need to live life gloriously, joyously, and abundantly.

                    A magnetized piece of steel will lift about twelve times its own weight, but if you demagnetize that same piece of steel, it will not even lift a feather. Similarly, there are two types of people: those who are magnetized, full of confidence and faith, and those who are demagnetized, full of fears and doubts.
                """.trimIndent()
            ),
            Chapter(
                id = 602L,
                bookId = 6L,
                number = 2,
                title = "How Your Own Mind Works",
                subtitle = "The Conscious vs Subconscious Dynamic",
                estimatedReadMinutes = 11,
                content = """
                    You have only one mind, but your mind possesses two distinct characteristics: the conscious (objective) mind and the subconscious (subjective) mind.

                    Think of your conscious mind as the gardener planting seeds, and your subconscious mind as the fertile soil. The soil does not judge whether the seed is an apple tree or deadly nightshade; it simply nurtures whatever is planted with emotional conviction. Whatever habit of thought you nurture consciously sinks into your subconscious and crystallizes into your physical environment.
                """.trimIndent()
            ),
            Chapter(
                id = 603L,
                bookId = 6L,
                number = 3,
                title = "Mental Healing in Modern Times",
                subtitle = "The Somatic Impact of Belief and Harmony",
                estimatedReadMinutes = 10,
                content = """
                    The subconscious mind controls all the vital processes of your physical body: circulation of blood, digestion, cellular regeneration, and respiration. It never sleeps, working continuously throughout the night.

                    When you flood your consciousness with ideas of harmony, health, and peace, the subconscious mind responds by restoring the biological equilibrium of the body. Faith is not blind superstition; it is a mental attitude of certitude that aligns subconscious agency with physical well-being.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(subconsciousBook, subconsciousChapters, ReadingProgress(6L, 601L, 15f)))

        val bodyScoreBook = Book(
            id = 7L,
            title = "The Body Keeps the Score",
            author = "Bessel van der Kolk",
            coverImageUri = "",
            rating = 4.9f,
            pageCount = 464,
            genre = "Psychology",
            description = "Brain, mind, and body in the healing of trauma. A groundbreaking work by one of the world's foremost experts on traumatic stress.",
            isImportedPdf = false,
            colorHex = "#2E5B88",
            accentTint = "#1E3B5A"
        )
        val bodyScoreChapters = listOf(
            Chapter(
                id = 701L,
                bookId = 7L,
                number = 1,
                title = "Lessons from Vietnam Veterans",
                subtitle = "Discovering the Neurobiology of Overwhelm",
                estimatedReadMinutes = 14,
                content = """
                    Trauma is not just an event that took place sometime in the past; it is also the imprint left by that experience on mind, brain, and body. This imprint has ongoing consequences for how the human organism manages and survives in the present.

                    Brain scans revealed that when trauma survivors are reminded of past events, the speech center of the brain (Broca’s area) shuts down, while the amygdala—the brain’s alarm system—fires intensely, throwing the autonomic nervous system into high alert. The body relives the past as if it were happening right now in the present moment.
                """.trimIndent()
            ),
            Chapter(
                id = 702L,
                bookId = 7L,
                number = 2,
                title = "This Is Your Brain on Trauma",
                subtitle = "The Smoke Detector and the Watchtower",
                estimatedReadMinutes = 12,
                content = """
                    The amygdala acts as the brain's smoke detector, identifying threats and flooding the bloodstream with cortisol and adrenaline before we can consciously think. The medial prefrontal cortex (MPFC) acts as the watchtower, looking down to assess whether the smoke is merely burnt toast or a real house fire.

                    In traumatized individuals, the connection between the smoke detector and the watchtower is impaired. The body remains perpetually mobilized for fight or flight, exhausting the cardiovascular and immune systems.
                """.trimIndent()
            ),
            Chapter(
                id = 703L,
                bookId = 7L,
                number = 3,
                title = "Paths to Recovery: Reclaiming the Body",
                subtitle = "EMDR, Somatic Experiencing, and Breath",
                estimatedReadMinutes = 15,
                content = """
                    Healing cannot occur purely through talking and rational analysis, because trauma is stored in the subcortical regions of the brain and in bodily sensations.

                    To recover, people must learn to feel what they feel and know what they know without being overwhelmed. Practices like rhythmic breathing, yoga, neurofeedback, theater improvisation, and EMDR (Eye Movement Desensitization and Reprocessing) enable individuals to safely reconnect with their somatic core, restoring feelings of safety, agency, and presence.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(bodyScoreBook, bodyScoreChapters, ReadingProgress(7L, 701L, 80f)))

        // ==========================================
        // 3. NOVELS CATEGORY
        // ==========================================
        val paperPalaceBook = Book(
            id = 8L,
            title = "The Paper Palace",
            author = "Miranda Cowley Heller",
            coverImageUri = "",
            rating = 4.5f,
            pageCount = 400,
            genre = "Novels",
            description = "A story of summer, secrets, and a decision that will change the course of a family's life forever on the shores of Cape Cod.",
            isImportedPdf = false,
            colorHex = "#4A6B53",
            accentTint = "#354E3C"
        )
        val paperPalaceChapters = listOf(
            Chapter(
                id = 801L,
                bookId = 8L,
                number = 1,
                title = "The Pond in July",
                subtitle = "Morning Mist and Silent Echoes",
                estimatedReadMinutes = 11,
                content = """
                    It is a perfect July morning, and Elle Bishop sits on the weathered wooden dock of the Paper Palace—the rustic family camp nestled in the backwoods of Cape Cod where she has spent every summer of her life.

                    The water of the freshwater pond is dark amber, stained by centuries of submerged oak leaves and pine needles. The fog is just beginning to burn off under the pale morning sun, revealing the gentle curve of sandy shallows. Elle can hear her children laughing faintly in the main cabin, her husband Peter brewing coffee on the antique stove, and yet her mind is miles away, tethered to the secret encounter of the night before.
                """.trimIndent()
            ),
            Chapter(
                id = 802L,
                bookId = 8L,
                number = 2,
                title = "Fifty Summers",
                subtitle = "Childhood Ghosts and Salt Air",
                estimatedReadMinutes = 13,
                content = """
                    The camp was constructed fifty years ago by her grandfather out of salvaged timber and compressed paperboard panels—which gave the place its whimsical name. Every splinter of cedar, every rusted screen door latch, and every threadbare quilt carries a generation of joy, betrayal, and unvoiced secrets.

                    Jonas was there across every single summer. They learned to sail sunfish boats together, caught snapping turtles with raw bacon on twine, and shared the tender, unspoken bond that only two children who survive familial wreckage can forge.
                """.trimIndent()
            ),
            Chapter(
                id = 803L,
                bookId = 8L,
                number = 3,
                title = "The Crossroads",
                subtitle = "Two Loves, One Irrevocable Choice",
                estimatedReadMinutes = 12,
                content = """
                    Now, over the next twenty-four hours, Elle must make an impossible choice between the life she has lovingly constructed with Peter—a loyal, brilliant man who brought calm to her stormy waters—and the visceral, destined love of Jonas that has haunted every beat of her heart since she was a girl.

                    As shadows lengthen across the pond, Elle realizes that forgiving herself is the only bridge to freedom.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(paperPalaceBook, paperPalaceChapters, ReadingProgress(8L, 801L, 30f)))

        val evelynHugoBook = Book(
            id = 9L,
            title = "The Seven Husbands of Evelyn Hugo",
            author = "Taylor Jenkins Reid",
            coverImageUri = "",
            rating = 4.8f,
            pageCount = 400,
            genre = "Novels",
            description = "Aging and reclusive Hollywood movie icon Evelyn Hugo is finally ready to tell the truth about her glamorous and scandalous life.",
            isImportedPdf = false,
            colorHex = "#285848",
            accentTint = "#1A3A30"
        )
        val evelynHugoChapters = listOf(
            Chapter(
                id = 901L,
                bookId = 9L,
                number = 1,
                title = "Poor Ernie Diaz",
                subtitle = "A Ticket Out of Hell's Kitchen",
                estimatedReadMinutes = 10,
                content = """
                    Evelyn Hugo sat in her Upper East Side penthouse, wrapped in emerald green silk, emerald eyes still as luminous as they were on silver screens in 1962.

                    "When you’re given the chance to tell your truth," she said to Monique, "you don’t hold back. People are going to love you or hate you regardless, so you might as well make sure they know the real monster or the real saint."

                    She began with Ernie Diaz, the kind neighborhood mechanic she married at sixteen simply to escape the stifling poverty of Hell's Kitchen and board a westbound Greyhound bus to Los Angeles. It was not cruel; it was survival in an era when young women had few paths to agency.
                """.trimIndent()
            ),
            Chapter(
                id = 902L,
                bookId = 9L,
                number = 2,
                title = "The Golden Age of Sunset",
                subtitle = "Dyeing the Hair, Crafting the Myth",
                estimatedReadMinutes = 12,
                content = """
                    In Hollywood, Evelyn Herrera became Evelyn Hugo. Studio executive Harry Cameron saw her fierce ambition and took her under his wing. The dark curls were bleached to champagne blonde, the eyebrows sculpted to arch with razor sharpness, and the voice lowered an octave.

                    She climbed from bit parts to starring roles through raw grit, understanding how to command every lens on the lot. Yet, behind the flashing paparazzi bulbs and velvet ropes, the Hollywood studio system was a ruthless machine that demanded total control over its stars' private lives.
                """.trimIndent()
            ),
            Chapter(
                id = 903L,
                bookId = 9L,
                number = 3,
                title = "Celia",
                subtitle = "The Great Love and the Greatest Secret",
                estimatedReadMinutes = 14,
                content = """
                    The seven husbands made headlines, but none of them held Evelyn's soul. That belonged to Celia St. James—the fiery, red-headed actress who rivaled her on screen and captivated her in secret.

                    In an era where exposure meant immediate career destruction and public ruin, their decades-long clandestine romance was a delicate dance of stolen weekends, heartbreaking separations, and profound devotion that outlasted all the men and fame Hollywood could bestow.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(evelynHugoBook, evelynHugoChapters, ReadingProgress(9L, 901L, 50f)))

        val vanishingHalfBook = Book(
            id = 10L,
            title = "The Vanishing Half",
            author = "Brit Bennett",
            coverImageUri = "",
            rating = 4.6f,
            pageCount = 352,
            genre = "Novels",
            description = "A stunning novel about twin sisters, inseparable as children, who ultimately choose to live in two very different worlds: one black and one white.",
            isImportedPdf = false,
            colorHex = "#664366",
            accentTint = "#4D324D"
        )
        val vanishingHalfChapters = listOf(
            Chapter(
                id = 1001L,
                bookId = 10L,
                number = 1,
                title = "The Vignes Twins of Mallard",
                subtitle = "Identical Faces, Divergent Fates",
                estimatedReadMinutes = 11,
                content = """
                    The town of Mallard, Louisiana, did not exist on any state map. Founded by freedmen who prized light skin above all else, its residents believed in breeding lighter with each generation like creaming coffee until the blackness vanished entirely.

                    Desiree and Stella Vignes grew up identical in every way: the same hazel eyes, sharp cheekbones, and soft laughter. Yet at sixteen, after fleeing their hometown for New Orleans, Stella vanished into thin air, leaving Desiree alone in the bus station with nothing but a single suitcase.
                """.trimIndent()
            ),
            Chapter(
                id = 1002L,
                bookId = 10L,
                number = 2,
                title = "Passing",
                subtitle = "The Hidden Architecture of White Brentwood",
                estimatedReadMinutes = 13,
                content = """
                    Years later, Stella is living in an affluent gated neighborhood in Brentwood, California, married to a wealthy white executive who has no idea about her ancestry. Every conversation, every social gathering is a high-wire performance where one misstep could shatter the illusion.

                    Stella lives with the constant terror of discovery, sacrificing her family and heritage for safety and privilege, while Desiree returns to Mallard with a dark-skinned daughter named Jude, confronting the very prejudices she once fled.
                """.trimIndent()
            ),
            Chapter(
                id = 1003L,
                bookId = 10L,
                number = 3,
                title = "Generations Intertwined",
                subtitle = "When the Daughters Meet in Los Angeles",
                estimatedReadMinutes = 12,
                content = """
                    Decades later, the twins' daughters—Jude, working her way through UCLA, and Kennedy, an aspiring blonde actress—cross paths by chance in a Los Angeles catering kitchen.

                    The inherited shadows of their mothers' decisions surface with quiet power, asking whether identity is something we are born into, or a story we construct for survival.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(vanishingHalfBook, vanishingHalfChapters, ReadingProgress(10L, 1001L, 0f)))

        val daisyJonesBook = Book(
            id = 11L,
            title = "Daisy Jones & The Six",
            author = "Taylor Jenkins Reid",
            coverImageUri = "",
            rating = 4.7f,
            pageCount = 368,
            genre = "Novels",
            description = "The rise and fall of a legendary 1970s rock band, told through candid oral histories of passion, music, addiction, and heartbreak.",
            isImportedPdf = false,
            colorHex = "#2E5D61",
            accentTint = "#1F3E41"
        )
        val daisyJonesChapters = listOf(
            Chapter(
                id = 1101L,
                bookId = 11L,
                number = 1,
                title = "The Sunset Strip in the 70s",
                subtitle = "Barefoot at the Whisky a Go Go",
                estimatedReadMinutes = 9,
                content = """
                    Daisy Jones was a girl walking barefoot down Sunset Boulevard in bell-bottoms, sneaking into clubs before she was old enough to drive. She had a raspy, spellbinding voice and a fierce refusal to be anybody's muse.

                    "I had no interest in being somebody else's muse. I am not a muse. I am the somebody. End of story."
                    — Daisy Jones

                    Meanwhile, Billy Dunne was sweating in Pittsburgh garages, driving his band—The Six—across the country in an old van, determined to conquer the rock and roll universe through sheer discipline and grit.
                """.trimIndent()
            ),
            Chapter(
                id = 1102L,
                bookId = 11L,
                number = 2,
                title = "The Aurora Sessions",
                subtitle = "Lightning in a Bottle at Sound City",
                estimatedReadMinutes = 12,
                content = """
                    When producer Teddy Price put Daisy and Billy into the same recording studio, the friction was instantaneous. They fought over lyrics, argued over tempo, and rewrote each other’s verses out of pride.

                    Yet that combustible tension produced pure alchemy. Their hit single 'Look at Us Now (Honeycomb)' launched the band into global stardom, leading to the creation of 'Aurora'—one of the most acclaimed albums in rock history, recorded in a haze of California sunshine and emotional fire.
                """.trimIndent()
            ),
            Chapter(
                id = 1103L,
                bookId = 11L,
                number = 3,
                title = "Soldier Field: The Final Encore",
                subtitle = "The Night the Music Stopped",
                estimatedReadMinutes = 11,
                content = """
                    On July 12, 1979, Daisy Jones & The Six played to ninety thousand screaming fans at Soldier Field in Chicago. They were at the absolute zenith of the music industry.

                    And then, without warning, they never played together again. In their own words decades later, the band members reveal why the music that brought them together was the very thing that broke them apart.
                """.trimIndent()
            )
        )
        books.add(SeedBookWithChapters(daisyJonesBook, daisyJonesChapters, ReadingProgress(11L, 1101L, 95f)))

        return books
    }
}
