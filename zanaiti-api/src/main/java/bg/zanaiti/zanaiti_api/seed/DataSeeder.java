package bg.zanaiti.zanaiti_api.seed;

import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CraftRepository craftRepository;
    private final CraftTranslationRepository craftTranslationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LanguageRepository languageRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizQuestionTranslationRepository quizQuestionTranslationRepository;
    private final UserProgressRepository userProgressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }
        // --- 1. Създаване на езици ---
        Language bg = Language.builder()
                .code("bg")
                .name("български")
                .isActive(true)
                .build();

        Language en = Language.builder()
                .code("en")
                .name("English")
                .isActive(true)
                .build();

        languageRepository.saveAll(List.of(bg, en));

        // --- 2. Създаване на роли ---
        Role userRole = Role.builder()
                .name("USER")
                .description("Regular user")
                .build();

        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrator")
                .build();

        roleRepository.saveAll(List.of(userRole, adminRole));

        // --- 3. Създаване на потребители ---
        User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Admin User")
                .totalPoints(0)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .roles(Set.of(adminRole, userRole))
                .build();

        User ivan = User.builder()
                .username("ivan")
                .email("ivan@example.com")
                .password(passwordEncoder.encode("pass123456"))
                .fullName("Ivan Ivanov")
                .totalPoints(0)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        User maria = User.builder()
                .username("maria")
                .email("maria@example.com")
                .password(passwordEncoder.encode("123456"))
                .fullName("Maria Petrova")
                .totalPoints(0)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.saveAll(List.of(admin, ivan, maria));

        // --- 4. Създаване на занаяти с преводи ---

        // Грънчарство / Pottery
        Craft pottery = Craft.builder()
                .imageUrl("https://images.unsplash.com/photo-1520408222757-6f9f95d87d5d?q=80&w=680&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                .animationUrl("https://cdn.pixabay.com/video/2020/07/24/45455-443133824_large.mp4")
                .latitude(42.0)
                .longitude(24.0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(admin)
                .build();
        craftRepository.save(pottery);

        CraftTranslation potteryBg = CraftTranslation.builder()
                .craft(pottery)
                .language(bg)
                .name("Грънчарство")
                .description("Традиционен български занаят за изработка на съдове от глина.")
                .historicalFacts("Грънчарството е един от най-старите занаяти по българските земи. " +
                        "Археологически находки свидетелстват за развита грънчарска традиция още от времето на траките.")
                .makingProcess("Глината се меси, оформя се на грънчарско колело, изсушава се и се пече в специална пещ.")
                .build();

        CraftTranslation potteryEn = CraftTranslation.builder()
                .craft(pottery)
                .language(en)
                .name("Pottery")
                .description("Traditional Bulgarian craft for making clay vessels.")
                .historicalFacts("Pottery is one of the oldest crafts in Bulgarian lands. " +
                        "Archaeological findings show developed pottery traditions since Thracian times.")
                .makingProcess("The clay is kneaded, shaped on a potter's wheel, dried, and fired in a special kiln.")
                .build();

        craftTranslationRepository.saveAll(List.of(potteryBg, potteryEn));

        // Тъкане / Weaving
        Craft weaving = Craft.builder()
                .imageUrl("https://images.unsplash.com/photo-1608724553456-89e963624dbb?q=80&w=874&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                .animationUrl("https://media.istockphoto.com/id/990939998/video/production-and-weaving-of-carpets-and-fabrics.mp4?s=mp4-640x640-is&k=20&c=clXJQBsbe7KQ4bNi9FBYXRteU-_yuAmseXGdCBm6f5c=")
                .latitude(41.9)
                .longitude(25.1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(admin)
                .build();
        craftRepository.save(weaving);

        CraftTranslation weavingBg = CraftTranslation.builder()
                .craft(weaving)
                .language(bg)
                .name("Тъкане")
                .description("Традиционно тъкане на ръчно изработени килими и платове.")
                .historicalFacts("Тъкането е важен занаят в българското Възраждане. " +
                        "Българските черги и килими са известни със своите уникални мотиви и цветове.")
                .makingProcess("Нишките се нареждат на стан, преплитат се с помощта на совалка и се създават различни десени.")
                .build();

        CraftTranslation weavingEn = CraftTranslation.builder()
                .craft(weaving)
                .language(en)
                .name("Weaving")
                .description("Traditional weaving of handmade carpets and fabrics.")
                .historicalFacts("Weaving was an important craft during the Bulgarian National Revival. " +
                        "Bulgarian rugs and carpets are known for their unique patterns and colors.")
                .makingProcess("Threads are arranged on a loom, interwoven with a shuttle, and various patterns are created.")
                .build();

        craftTranslationRepository.saveAll(List.of(weavingBg, weavingEn));

        // --- 5. Създаване на въпроси с преводи ---
        createPotteryQuestions(pottery, bg, en);
        createWeavingQuestions(weaving, bg, en);

        System.out.println("Seed data loaded successfully!");
        System.out.println("   - Languages: bg, en");
        System.out.println("   - Roles: USER, ADMIN");
        System.out.println("   - Users: admin, ivan, maria");
        System.out.println("   - Crafts: Pottery, Weaving (with translations)");
        System.out.println("   - Quiz questions: 10 (with translations)");
    }

    private void createPotteryQuestions(Craft pottery, Language bg, Language en) {

        // Въпрос 1
        QuizQuestion q1 = QuizQuestion.builder()
                .craft(pottery)
                .correctOptionIndex(1) // B = 1 (0-A,1-B,2-C,3-D)
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q1);

        QuizQuestionTranslation q1Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q1)
                .language(bg)
                .questionText("Какъв материал се използва за грънчарство?")
                .optionA("Дърво")
                .optionB("Глина")
                .optionC("Метал")
                .optionD("Камък")
                .build();

        QuizQuestionTranslation q1En = QuizQuestionTranslation.builder()
                .quizQuestion(q1)
                .language(en)
                .questionText("What material is used for pottery?")
                .optionA("Wood")
                .optionB("Clay")
                .optionC("Metal")
                .optionD("Stone")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q1Bg, q1En));

        // Въпрос 2
        QuizQuestion q2 = QuizQuestion.builder()
                .craft(pottery)
                .correctOptionIndex(0) // A
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q2);

        QuizQuestionTranslation q2Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q2)
                .language(bg)
                .questionText("Как се оформя съд от глина?")
                .optionA("С помощта на грънчарско колело")
                .optionB("Чрез шприцоване")
                .optionC("Чрез рязане")
                .optionD("Чрез лепене")
                .build();

        QuizQuestionTranslation q2En = QuizQuestionTranslation.builder()
                .quizQuestion(q2)
                .language(en)
                .questionText("How is a clay vessel shaped?")
                .optionA("Using a potter's wheel")
                .optionB("By injection")
                .optionC("By cutting")
                .optionD("By gluing")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q2Bg, q2En));

        // Въпрос 3
        QuizQuestion q3 = QuizQuestion.builder()
                .craft(pottery)
                .correctOptionIndex(0) // A
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q3);

        QuizQuestionTranslation q3Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q3)
                .language(bg)
                .questionText("Как се изсушава глинен съд преди печене?")
                .optionA("На слънце")
                .optionB("Във вода")
                .optionC("В пясък")
                .optionD("В хладилник")
                .build();

        QuizQuestionTranslation q3En = QuizQuestionTranslation.builder()
                .quizQuestion(q3)
                .language(en)
                .questionText("How is a clay vessel dried before firing?")
                .optionA("In the sun")
                .optionB("In water")
                .optionC("In sand")
                .optionD("In a refrigerator")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q3Bg, q3En));

        // Въпрос 4
        QuizQuestion q4 = QuizQuestion.builder()
                .craft(pottery)
                .correctOptionIndex(1) // B
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q4);

        QuizQuestionTranslation q4Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q4)
                .language(bg)
                .questionText("Как се получава глазура върху глинен съд?")
                .optionA("С боядисване")
                .optionB("С печене с глазур")
                .optionC("С полиране")
                .optionD("С втвърдяване")
                .build();

        QuizQuestionTranslation q4En = QuizQuestionTranslation.builder()
                .quizQuestion(q4)
                .language(en)
                .questionText("How is glaze applied to a clay vessel?")
                .optionA("By painting")
                .optionB("By firing with glaze")
                .optionC("By polishing")
                .optionD("By hardening")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q4Bg, q4En));

        // Въпрос 5
        QuizQuestion q5 = QuizQuestion.builder()
                .craft(pottery)
                .correctOptionIndex(1) // B
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q5);

        QuizQuestionTranslation q5Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q5)
                .language(bg)
                .questionText("Какво е основното предназначение на грънчарството?")
                .optionA("Декорация")
                .optionB("Производство на съдове")
                .optionC("Скулптура")
                .optionD("Облекло")
                .build();

        QuizQuestionTranslation q5En = QuizQuestionTranslation.builder()
                .quizQuestion(q5)
                .language(en)
                .questionText("What is the main purpose of pottery?")
                .optionA("Decoration")
                .optionB("Production of vessels")
                .optionC("Sculpture")
                .optionD("Clothing")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q5Bg, q5En));
    }

    private void createWeavingQuestions(Craft weaving, Language bg, Language en) {

        // Въпрос 1
        QuizQuestion q1 = QuizQuestion.builder()
                .craft(weaving)
                .correctOptionIndex(1) // B
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q1);

        QuizQuestionTranslation q1Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q1)
                .language(bg)
                .questionText("Какво се използва за тъкане на платове?")
                .optionA("Глина")
                .optionB("Вълна")
                .optionC("Метал")
                .optionD("Пясък")
                .build();

        QuizQuestionTranslation q1En = QuizQuestionTranslation.builder()
                .quizQuestion(q1)
                .language(en)
                .questionText("What is used for weaving fabrics?")
                .optionA("Clay")
                .optionB("Wool")
                .optionC("Metal")
                .optionD("Sand")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q1Bg, q1En));

        // Въпрос 2
        QuizQuestion q2 = QuizQuestion.builder()
                .craft(weaving)
                .correctOptionIndex(1) // B
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q2);

        QuizQuestionTranslation q2Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q2)
                .language(bg)
                .questionText("Какъв инструмент се използва за тъкане?")
                .optionA("Чук")
                .optionB("Стан")
                .optionC("Мелница")
                .optionD("Нож")
                .build();

        QuizQuestionTranslation q2En = QuizQuestionTranslation.builder()
                .quizQuestion(q2)
                .language(en)
                .questionText("What tool is used for weaving?")
                .optionA("Hammer")
                .optionB("Loom")
                .optionC("Mill")
                .optionD("Knife")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q2Bg, q2En));

        // Въпрос 3
        QuizQuestion q3 = QuizQuestion.builder()
                .craft(weaving)
                .correctOptionIndex(1) // B
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q3);

        QuizQuestionTranslation q3Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q3)
                .language(bg)
                .questionText("Какво е типично за ръчно тъканите платове?")
                .optionA("Еднородност")
                .optionB("Уникални мотиви")
                .optionC("Метални нишки")
                .optionD("Глина")
                .build();

        QuizQuestionTranslation q3En = QuizQuestionTranslation.builder()
                .quizQuestion(q3)
                .language(en)
                .questionText("What is typical for hand-woven fabrics?")
                .optionA("Uniformity")
                .optionB("Unique patterns")
                .optionC("Metal threads")
                .optionD("Clay")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q3Bg, q3En));

        // Въпрос 4
        QuizQuestion q4 = QuizQuestion.builder()
                .craft(weaving)
                .correctOptionIndex(0) // A
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q4);

        QuizQuestionTranslation q4Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q4)
                .language(bg)
                .questionText("Как се закрепват нишките върху стан?")
                .optionA("С връзки")
                .optionB("С лепило")
                .optionC("С тел")
                .optionD("С мъниста")
                .build();

        QuizQuestionTranslation q4En = QuizQuestionTranslation.builder()
                .quizQuestion(q4)
                .language(en)
                .questionText("How are threads attached to a loom?")
                .optionA("With ties")
                .optionB("With glue")
                .optionC("With wire")
                .optionD("With beads")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q4Bg, q4En));

        // Въпрос 5
        QuizQuestion q5 = QuizQuestion.builder()
                .craft(weaving)
                .correctOptionIndex(0) // A
                .pointsReward(10)
                .isActive(true)
                .build();
        quizQuestionRepository.save(q5);

        QuizQuestionTranslation q5Bg = QuizQuestionTranslation.builder()
                .quizQuestion(q5)
                .language(bg)
                .questionText("Коя е основната цел на тъкането?")
                .optionA("Производство на текстил")
                .optionB("Изработка на съдове")
                .optionC("Грънчарство")
                .optionD("Писане")
                .build();

        QuizQuestionTranslation q5En = QuizQuestionTranslation.builder()
                .quizQuestion(q5)
                .language(en)
                .questionText("What is the main purpose of weaving?")
                .optionA("Textile production")
                .optionB("Making vessels")
                .optionC("Pottery")
                .optionD("Writing")
                .build();
        quizQuestionTranslationRepository.saveAll(List.of(q5Bg, q5En));
    }
}