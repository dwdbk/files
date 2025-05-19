// Project Structure for Operas Service
// src/main/java/com/operamanagement/operasservice/

// 1. MAVEN POM.XML
// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.operamanagement</groupId>
    <artifactId>operas-service</artifactId>
    <version>1.0.0</version>
    <name>operas-service</name>
    <description>Operas Management Service</description>

    <properties>
        <java.version>17</java.version>
        <dgs.version>7.5.0</dgs.version>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Netflix DGS -->
        <dependency>
            <groupId>com.netflix.graphql.dgs</groupId>
            <artifactId>graphql-dgs-spring-boot-starter</artifactId>
            <version>${dgs.version}</version>
        </dependency>
        <dependency>
            <groupId>com.netflix.graphql.dgs</groupId>
            <artifactId>graphql-dgs-extended-scalars</artifactId>
            <version>${dgs.version}</version>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.netflix.graphql.dgs</groupId>
            <artifactId>graphql-dgs-client</artifactId>
            <version>${dgs.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>com.netflix.graphql.dgs</groupId>
                <artifactId>graphql-dgs-codegen-gradle</artifactId>
                <version>${dgs.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <schemaPaths>
                                <param>src/main/resources/schema</param>
                            </schemaPaths>
                            <packageName>com.operamanagement.operasservice.generated</packageName>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

// 2. DOMAIN MODELS (JPA ENTITIES)
// src/main/java/com/operamanagement/operasservice/domain/Opera.java
package com.operamanagement.operasservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Opera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotNull(message = "Creation date is required")
    private LocalDate creationDate;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "opera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Act> acts = new ArrayList<>();

    @OneToMany(mappedBy = "opera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scene> scenes = new ArrayList<>();
}

// src/main/java/com/operamanagement/operasservice/domain/Act.java
package com.operamanagement.operasservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "acts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Act {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String description;

    @NotNull
    private Integer actNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opera_id", nullable = false)
    private Opera opera;
}

// src/main/java/com/operamanagement/operasservice/domain/Scene.java
package com.operamanagement.operasservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer sceneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opera_id", nullable = false)
    private Opera opera;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Character> characters = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decor_id")
    private Decor decor;
}

// src/main/java/com/operamanagement/operasservice/domain/Character.java
package com.operamanagement.operasservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "characters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Column(name = "is_main")
    private Boolean isMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;
}

// src/main/java/com/operamanagement/operasservice/domain/Decor.java
package com.operamanagement.operasservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Decor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    private String score;

    @OneToMany(mappedBy = "decor", cascade = CascadeType.ALL)
    private List<Scene> scenes = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "decor_music",
        joinColumns = @JoinColumn(name = "decor_id"),
        inverseJoinColumns = @JoinColumn(name = "music_id")
    )
    private List<Music> features = new ArrayList<>();
}

// src/main/java/com/operamanagement/operasservice/domain/Music.java
package com.operamanagement.operasservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "music")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    private String score;

    @ManyToMany(mappedBy = "features")
    private List<Decor> decors = new ArrayList<>();
}

// 3. REPOSITORIES
// src/main/java/com/operamanagement/operasservice/repository/OperaRepository.java
package com.operamanagement.operasservice.repository;

import com.operamanagement.operasservice.domain.Opera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperaRepository extends JpaRepository<Opera, Long> {
}

// src/main/java/com/operamanagement/operasservice/repository/ActRepository.java
package com.operamanagement.operasservice.repository;

import com.operamanagement.operasservice.domain.Act;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActRepository extends JpaRepository<Act, Long> {
    List<Act> findByOperaId(Long operaId);
}

// src/main/java/com/operamanagement/operasservice/repository/SceneRepository.java
package com.operamanagement.operasservice.repository;

import com.operamanagement.operasservice.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByOperaId(Long operaId);
}

// src/main/java/com/operamanagement/operasservice/repository/CharacterRepository.java
package com.operamanagement.operasservice.repository;

import com.operamanagement.operasservice.domain.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findBySceneId(Long sceneId);
}

// src/main/java/com/operamanagement/operasservice/repository/DecorRepository.java
package com.operamanagement.operasservice.repository;

import com.operamanagement.operasservice.domain.Decor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecorRepository extends JpaRepository<Decor, Long> {
}

// src/main/java/com/operamanagement/operasservice/repository/MusicRepository.java
package com.operamanagement.operasservice.repository;

import com.operamanagement.operasservice.domain.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    List<Music> findByDecorsId(Long decorId);
}

// 4. SERVICES
// src/main/java/com/operamanagement/operasservice/service/OperaService.java
package com.operamanagement.operasservice.service;

import com.operamanagement.operasservice.domain.Opera;
import com.operamanagement.operasservice.exception.ResourceNotFoundException;
import com.operamanagement.operasservice.repository.OperaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperaService {
    private final OperaRepository operaRepository;

    public List<Opera> getAllOperas() {
        return operaRepository.findAll();
    }

    public Opera getOperaById(Long id) {
        return operaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opera not found with id: " + id));
    }

    @Transactional
    public Opera createOpera(Opera opera) {
        return operaRepository.save(opera);
    }

    @Transactional
    public Opera updateOpera(Long id, Opera operaDetails) {
        Opera opera = getOperaById(id);
        opera.setTitle(operaDetails.getTitle());
        opera.setAuthor(operaDetails.getAuthor());
        opera.setCreationDate(operaDetails.getCreationDate());
        opera.setDuration(operaDetails.getDuration());
        opera.setDescription(operaDetails.getDescription());
        return operaRepository.save(opera);
    }

    @Transactional
    public void deleteOpera(Long id) {
        Opera opera = getOperaById(id);
        operaRepository.delete(opera);
    }
}

// src/main/java/com/operamanagement/operasservice/service/ActService.java
package com.operamanagement.operasservice.service;

import com.operamanagement.operasservice.domain.Act;
import com.operamanagement.operasservice.domain.Opera;
import com.operamanagement.operasservice.exception.ResourceNotFoundException;
import com.operamanagement.operasservice.repository.ActRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActService {
    private final ActRepository actRepository;
    private final OperaService operaService;

    public List<Act> getAllActs() {
        return actRepository.findAll();
    }

    public List<Act> getActsByOperaId(Long operaId) {
        return actRepository.findByOperaId(operaId);
    }

    public Act getActById(Long id) {
        return actRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Act not found with id: " + id));
    }

    @Transactional
    public Act createAct(Act act, Long operaId) {
        Opera opera = operaService.getOperaById(operaId);
        act.setOpera(opera);
        return actRepository.save(act);
    }

    @Transactional
    public Act updateAct(Long id, Act actDetails) {
        Act act = getActById(id);
        act.setDescription(actDetails.getDescription());
        act.setActNumber(actDetails.getActNumber());
        return actRepository.save(act);
    }

    @Transactional
    public void deleteAct(Long id) {
        Act act = getActById(id);
        actRepository.delete(act);
    }
}

// Other service classes for Scene, Character, Decor, and Music follow the same pattern
// For brevity, I'll continue with the GraphQL schemas and resolvers

// 5. GRAPHQL SCHEMA
// src/main/resources/schema/schema.graphqls
directive @key(fields: String!) on OBJECT
directive @extends on OBJECT
directive @external on FIELD_DEFINITION
directive @requires(fields: String!) on FIELD_DEFINITION
directive @provides(fields: String!) on FIELD_DEFINITION

type Query {
    operas: [Opera]
    opera(id: ID!): Opera
    acts: [Act]
    act(id: ID!): Act
    scenes: [Scene]
    scene(id: ID!): Scene
    characters: [Character]
    character(id: ID!): Character
    decors: [Decor]
    decor(id: ID!): Decor
    musics: [Music]
    music(id: ID!): Music
}

type Mutation {
    createOpera(input: OperaInput!): Opera
    updateOpera(id: ID!, input: OperaInput!): Opera
    deleteOpera(id: ID!): Boolean
    
    createAct(input: ActInput!): Act
    updateAct(id: ID!, input: ActInput!): Act
    deleteAct(id: ID!): Boolean
    
    createScene(input: SceneInput!): Scene
    updateScene(id: ID!, input: SceneInput!): Scene
    deleteScene(id: ID!): Boolean
    
    createCharacter(input: CharacterInput!): Character
    updateCharacter(id: ID!, input: CharacterInput!): Character
    deleteCharacter(id: ID!): Boolean
    
    createDecor(input: DecorInput!): Decor
    updateDecor(id: ID!, input: DecorInput!): Decor
    deleteDecor(id: ID!): Boolean
    
    createMusic(input: MusicInput!): Music
    updateMusic(id: ID!, input: MusicInput!): Music
    deleteMusic(id: ID!): Boolean
}

scalar Date

type Opera @key(fields: "id") {
    id: ID!
    title: String!
    author: String!
    creationDate: Date!
    duration: Int!
    description: String
    acts: [Act]
    scenes: [Scene]
}

type Act {
    id: ID!
    description: String
    actNumber: Int!
    opera: Opera!
}

type Scene {
    id: ID!
    sceneNumber: Int!
    opera: Opera!
    characters: [Character]
    decor: Decor
}

type Character {
    id: ID!
    name: String!
    isMain: Boolean
    scene: Scene!
}

type Decor {
    id: ID!
    title: String!
    description: String
    lyrics: String
    score: String
    scenes: [Scene]
    features: [Music]
}

type Music {
    id: ID!
    title: String!
    description: String
    lyrics: String
    score: String
    decors: [Decor]
}

input OperaInput {
    title: String!
    author: String!
    creationDate: Date!
    duration: Int!
    description: String
}

input ActInput {
    description: String
    actNumber: Int!
    operaId: ID!
}

input SceneInput {
    sceneNumber: Int!
    operaId: ID!
    decorId: ID
}

input CharacterInput {
    name: String!
    isMain: Boolean
    sceneId: ID!
}

input DecorInput {
    title: String!
    description: String
    lyrics: String
    score: String
}

input MusicInput {
    title: String!
    description: String
    lyrics: String
    score: String
    decorIds: [ID]
}

// 6. GRAPHQL RESOLVERS
// src/main/java/com/operamanagement/operasservice/graphql/OperaDataFetcher.java
package com.operamanagement.operasservice.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.operamanagement.operasservice.domain.Opera;
import com.operamanagement.operasservice.domain.Act;
import com.operamanagement.operasservice.domain.Scene;
import com.operamanagement.operasservice.service.OperaService;
import com.operamanagement.operasservice.service.ActService;
import com.operamanagement.operasservice.service.SceneService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class OperaDataFetcher {
    private final OperaService operaService;
    private final ActService actService;
    private final SceneService sceneService;

    @DgsQuery
    public List<Opera> operas() {
        return operaService.getAllOperas();
    }

    @DgsQuery
    public Opera opera(@InputArgument Long id) {
        return operaService.getOperaById(id);
    }

    @DgsData(parentType = "Opera", field = "acts")
    public List<Act> getActsForOpera(DgsDataFetchingEnvironment dfe) {
        Opera opera = dfe.getSource();
        return actService.getActsByOperaId(opera.getId());
    }

    @DgsData(parentType = "Opera", field = "scenes")
    public List<Scene> getScenesForOpera(DgsDataFetchingEnvironment dfe) {
        Opera opera = dfe.getSource();
        return sceneService.getScenesByOperaId(opera.getId());
    }

    @DgsMutation
    public Opera createOpera(@InputArgument("input") Map<String, Object> input) {
        Opera opera = new Opera();
        opera.setTitle((String) input.get("title"));
        opera.setAuthor((String) input.get("author"));
        opera.setCreationDate(LocalDate.parse((String) input.get("creationDate")));
        opera.setDuration((Integer) input.get("duration"));
        opera.setDescription((String) input.get("description"));
        return operaService.createOpera(opera);
    }

    @DgsMutation
    public Opera updateOpera(@InputArgument Long id, @InputArgument("input") Map<String, Object> input) {
        Opera opera = new Opera();
        opera.setTitle((String) input.get("title"));
        opera.setAuthor((String) input.get("author"));
        opera.setCreationDate(LocalDate.parse((String) input.get("creationDate")));
        opera.setDuration((Integer) input.get("duration"));
        opera.setDescription((String) input.get("description"));
        return operaService.updateOpera(id, opera);
    }

    @DgsMutation
    public Boolean deleteOpera(@InputArgument Long id) {
        operaService.deleteOpera(id);
        return true;
    }
}

// src/main/java/com/operamanagement/operasservice/graphql/ActDataFetcher.java
package com.operamanagement.operasservice.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.operamanagement.operasservice.domain.Act;
import com.operamanagement.operasservice.domain.Opera;
import com.operamanagement.operasservice.service.ActService;
import com.operamanagement.operasservice.service.OperaService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class ActDataFetcher {
    private final ActService actService;
    private final OperaService operaService;

    @DgsQuery
    public List<Act> acts() {
        return actService.getAllActs();
    }

    @DgsQuery
    public Act act(@InputArgument Long id) {
        return actService.getActById(id);
    }

    @DgsData(parentType = "Act", field = "opera")
    public Opera getOperaForAct(DgsDataFetchingEnvironment dfe) {
        Act act = dfe.getSource();
        return operaService.getOperaById(act.getOpera().getId());
    }

    @DgsMutation
    public Act createAct(@InputArgument("input") Map<String, Object> input) {
        Act act = new Act();
        act.setDescription((String) input.get("description"));
        act.setActNumber((Integer) input.get("actNumber"));
        Long operaId = Long.valueOf((String) input.get("operaId"));
        return actService.createAct(act, operaId);
    }

    @DgsMutation
    public Act updateAct(@InputArgument Long id, @InputArgument("input") Map<String, Object> input) {
        Act act = new Act();
        act.setDescription((String) input.get("description"));
        act.setActNumber((Integer) input.get("actNumber"));
        return actService.updateAct(id, act);
    }

    @DgsMutation
    public Boolean deleteAct(@InputArgument Long id) {
        actService.deleteAct(id);
        return true;
    }
}

// Similar pattern for other resolvers (Scene, Character, Decor, Music)

// 7. EXCEPTION HANDLING
// src/main/java/com/operamanagement/operasservice/exception/ResourceNotFoundException.java
package com.operamanagement.operasservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// src/main/java/com/operamanagement/operasservice/exception/GraphQLExceptionHandler.java
package com.operamanagement.operasservice.exception;

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class GraphQLExceptionHandler extends DefaultDataFetcherExceptionHandler {
    @Override
    public CompletableFuture<List<GraphQLError>> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
        if (handlerParameters.getException() instanceof ResourceNotFoundException) {
            GraphQLError error = TypedGraphQLError.newNotFoundBuilder()
                    .message(handlerParameters.getException().getMessage())
                    .path(handlerParameters.getPath())
                    .build();
            return CompletableFuture.completedFuture(List.of(error));
        }
        return super.handleException(handlerParameters);
    }
}

// 8. APPLICATION CONFIGURATION
// src/main/java/com/operamanagement/operasservice/OperasServiceApplication.java
package com.operamanagement.operasservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OperasServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperasServiceApplication.class, args);
    }
}

// src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/opera_management
spring.datasource.username