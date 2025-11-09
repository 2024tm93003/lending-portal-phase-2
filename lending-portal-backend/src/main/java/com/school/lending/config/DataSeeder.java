package com.school.lending.config;

import com.school.lending.model.Equipment;
import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.repo.EquipmentRepository;
import com.school.lending.repo.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds initial data into the database on application startup.
 *
 * <p>This component inserts a few sample users and equipment items when the
 * corresponding repositories are empty. It implements {@link CommandLineRunner}
 * so seeding runs after the Spring context is initialized.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserAccountRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserAccountRepository userRepository, EquipmentRepository equipmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Insert seed users and equipment when the repositories are empty.
     *
     * @param args startup arguments (ignored)
     */
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new UserAccount("ram", passwordEncoder.encode("ram@123"), UserRole.STUDENT, "Sai Ram"));
            userRepository.save(new UserAccount("suresh", passwordEncoder.encode("suresh@123"), UserRole.STAFF, "Suresh Babu"));
            userRepository.save(new UserAccount("prakash", passwordEncoder.encode("prakash@123"), UserRole.ADMIN, "Prakash Raj"));
        }

        if (equipmentRepository.count() == 0) {
            equipmentRepository.save(new Equipment("Canon EOS 80D", "Camera", "Needs strap replacement", 5, 5));
            equipmentRepository.save(new Equipment("Basketball Kit", "Sports", "Used but intact", 20, 18));
            equipmentRepository.save(new Equipment("Chemistry Lab Set", "Lab", "Glassware missing 2 test tubes", 10, 9));
            equipmentRepository.save(new Equipment("Acoustic Guitar", "Music", "Strings replaced recently", 3, 3));
        }
    }
}
