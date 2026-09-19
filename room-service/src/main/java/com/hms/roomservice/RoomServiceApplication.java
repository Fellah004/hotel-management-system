package com.hms.roomservice;

import com.hms.roomservice.entity.*;
import com.hms.roomservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Set;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class RoomServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initRoomData(
            RoomCategoryRepository categoryRepository,
            AmenityRepository amenityRepository,
            RoomRepository roomRepository,
            HallCategoryRepository hallCategoryRepository,
            HallRepository hallRepository) {
        return args -> {
            Amenity wifi = null;
            Amenity tv = null;
            Amenity minibar = null;
            Amenity jacuzzi = null;
            Amenity projector = null;
            Amenity soundSystem = null;

            if (amenityRepository.count() == 0) {
                wifi = amenityRepository.save(Amenity.builder().name("High-Speed WiFi").description("Complimentary high-speed WiFi").icon("wifi").build());
                tv = amenityRepository.save(Amenity.builder().name("Smart TV").description("55-inch 4K Smart TV").icon("tv").build());
                minibar = amenityRepository.save(Amenity.builder().name("Mini Bar").description("Stocked premium minibar").icon("coffee").build());
                jacuzzi = amenityRepository.save(Amenity.builder().name("Jacuzzi").description("Private luxury jacuzzi").icon("bath").build());
                projector = amenityRepository.save(Amenity.builder().name("4K Laser Projector").description("Motorized 4K projector & drop-down screen").icon("projector").build());
                soundSystem = amenityRepository.save(Amenity.builder().name("Pro Audio & Wireless Mics").description("JBL surround sound with wireless microphones").icon("mic").build());
            }

            if (categoryRepository.count() == 0) {
                RoomCategory single = categoryRepository.save(RoomCategory.builder()
                        .name("SINGLE")
                        .description("Standard single room for 1 guest")
                        .basePrice(new BigDecimal("100.00"))
                        .maxOccupancy(1)
                        .build());

                RoomCategory doubleCat = categoryRepository.save(RoomCategory.builder()
                        .name("DOUBLE")
                        .description("Comfortable double room for 2 guests")
                        .basePrice(new BigDecimal("160.00"))
                        .maxOccupancy(2)
                        .build());

                RoomCategory deluxe = categoryRepository.save(RoomCategory.builder()
                        .name("DELUXE")
                        .description("Deluxe room with luxury king bed and balcony")
                        .basePrice(new BigDecimal("250.00"))
                        .maxOccupancy(3)
                        .build());

                RoomCategory suite = categoryRepository.save(RoomCategory.builder()
                        .name("SUITE")
                        .description("Presidential luxury suite with ocean view and lounge")
                        .basePrice(new BigDecimal("450.00"))
                        .maxOccupancy(4)
                        .build());

                if (wifi != null && tv != null) {
                    // Seed initial lodging rooms
                    roomRepository.save(Room.builder()
                            .roomNumber("101")
                            .category(single)
                            .floor(1)
                            .capacity(1)
                            .pricePerNight(new BigDecimal("100.00"))
                            .status(RoomStatus.AVAILABLE)
                            .active(true)
                            .amenities(Set.of(wifi, tv))
                            .build());

                    roomRepository.save(Room.builder()
                            .roomNumber("102")
                            .category(doubleCat)
                            .floor(1)
                            .capacity(2)
                            .pricePerNight(new BigDecimal("160.00"))
                            .status(RoomStatus.AVAILABLE)
                            .active(true)
                            .amenities(Set.of(wifi, tv))
                            .build());

                    roomRepository.save(Room.builder()
                            .roomNumber("201")
                            .category(deluxe)
                            .floor(2)
                            .capacity(3)
                            .pricePerNight(new BigDecimal("250.00"))
                            .status(RoomStatus.AVAILABLE)
                            .active(true)
                            .amenities(minibar != null ? Set.of(wifi, tv, minibar) : Set.of(wifi, tv))
                            .build());

                    roomRepository.save(Room.builder()
                            .roomNumber("301")
                            .category(suite)
                            .floor(3)
                            .capacity(4)
                            .pricePerNight(new BigDecimal("450.00"))
                            .status(RoomStatus.AVAILABLE)
                            .active(true)
                            .amenities(jacuzzi != null ? Set.of(wifi, tv, minibar, jacuzzi) : Set.of(wifi, tv))
                            .build());
                }
            }

            if (hallCategoryRepository.count() == 0) {
                HallCategory banquet = hallCategoryRepository.save(HallCategory.builder()
                        .name("BANQUET_HALL")
                        .description("Grand banquet hall suitable for weddings, galas, and major corporate events")
                        .basePricePerHour(new BigDecimal("200.00"))
                        .basePricePerDay(new BigDecimal("1800.00"))
                        .minCapacity(50)
                        .maxCapacity(500)
                        .build());

                HallCategory conference = hallCategoryRepository.save(HallCategory.builder()
                        .name("CONFERENCE_HALL")
                        .description("Modern conference hall equipped for seminars, trainings, and workshops")
                        .basePricePerHour(new BigDecimal("100.00"))
                        .basePricePerDay(new BigDecimal("800.00"))
                        .minCapacity(20)
                        .maxCapacity(150)
                        .build());

                HallCategory boardroom = hallCategoryRepository.save(HallCategory.builder()
                        .name("BOARD_ROOM")
                        .description("Executive executive boardroom with video conferencing for VIP meetings")
                        .basePricePerHour(new BigDecimal("75.00"))
                        .basePricePerDay(new BigDecimal("500.00"))
                        .minCapacity(5)
                        .maxCapacity(25)
                        .build());

                if (hallRepository.count() == 0) {
                    hallRepository.save(Hall.builder()
                            .hallNumber("HALL-A")
                            .name("Grand Ballroom")
                            .category(banquet)
                            .floor(1)
                            .totalAreaSqFt(5000)
                            .theaterCapacity(450)
                            .uShapeCapacity(100)
                            .clusterCapacity(300)
                            .classroomCapacity(200)
                            .pricePerHour(new BigDecimal("250.00"))
                            .pricePerDay(new BigDecimal("2000.00"))
                            .status(HallStatus.AVAILABLE)
                            .active(true)
                            .amenities(projector != null && soundSystem != null ? Set.of(wifi, projector, soundSystem) : (wifi != null ? Set.of(wifi) : Set.of()))
                            .build());

                    hallRepository.save(Hall.builder()
                            .hallNumber("HALL-B")
                            .name("Sapphire Conference Center")
                            .category(conference)
                            .floor(2)
                            .totalAreaSqFt(2200)
                            .theaterCapacity(120)
                            .uShapeCapacity(45)
                            .clusterCapacity(70)
                            .classroomCapacity(60)
                            .pricePerHour(new BigDecimal("120.00"))
                            .pricePerDay(new BigDecimal("950.00"))
                            .status(HallStatus.AVAILABLE)
                            .active(true)
                            .amenities(projector != null ? Set.of(wifi, projector) : (wifi != null ? Set.of(wifi) : Set.of()))
                            .build());

                    hallRepository.save(Hall.builder()
                            .hallNumber("BOARD-1")
                            .name("Executive Boardroom")
                            .category(boardroom)
                            .floor(3)
                            .totalAreaSqFt(800)
                            .theaterCapacity(20)
                            .uShapeCapacity(16)
                            .clusterCapacity(16)
                            .classroomCapacity(16)
                            .pricePerHour(new BigDecimal("80.00"))
                            .pricePerDay(new BigDecimal("550.00"))
                            .status(HallStatus.AVAILABLE)
                            .active(true)
                            .amenities(projector != null ? Set.of(wifi, tv, projector) : (wifi != null ? Set.of(wifi) : Set.of()))
                            .build());
                }
            }
        };
    }
}
