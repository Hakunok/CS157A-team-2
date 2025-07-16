package com.airchive.dao;

import com.airchive.model.TopicInteraction;
import com.airchive.model.TopicInteraction.InteractionType;
import com.airchive.util.AppContextProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

public class TopicInteractionDAO {
  private final DataSource dataSource;

  public TopicInteractionDAO() {
    ServletContext context = AppContextProvider.getServletContext();
    this.dataSource = (DataSource) context.getAttribute("dataSource");
  }

  public TopicInteraction create(TopicInteraction interaction) throws SQLException {
    String sql = "INSERT INTO topic_interaction (user_id, topic_id, interaction, interacted_at) " +
        "VALUES (?, ?, ?, ?)";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      stmt.setInt(1, interaction.getUserId());
      stmt.setInt(2, interaction.getTopicId());
      stmt.setString(3, interaction.getInteraction().name());
      stmt.setTimestamp(4, Timestamp.valueOf(interaction.getInteractedAt()));

      stmt.executeUpdate();
      ResultSet keys = stmt.getGeneratedKeys();
      if (keys.next()) {
        interaction.setInteractionId(keys.getInt(1));
      }

      return interaction;
    }
  }

  public List<TopicInteraction> findByUserId(int userId) throws SQLException {
    String sql = "SELECT * FROM topic_interaction WHERE user_id = ? ORDER BY interacted_at DESC";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();
      List<TopicInteraction> list = new ArrayList<>();
      while (rs.next()) {
        list.add(mapRow(rs));
      }
      return list;
    }
  }

  public List<TopicInteraction> findByTopicId(int topicId) throws SQLException {
    String sql = "SELECT * FROM topic_interaction WHERE topic_id = ? ORDER BY interacted_at DESC";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, topicId);
      ResultSet rs = stmt.executeQuery();
      List<TopicInteraction> list = new ArrayList<>();
      while (rs.next()) {
        list.add(mapRow(rs));
      }
      return list;
    }
  }

  public List<TopicInteraction> findByUserAndType(int userId, InteractionType type) throws SQLException {
    String sql = "SELECT * FROM topic_interaction WHERE user_id = ? AND interaction = ? ORDER BY interacted_at DESC";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      stmt.setString(2, type.name());

      ResultSet rs = stmt.executeQuery();
      List<TopicInteraction> list = new ArrayList<>();
      while (rs.next()) {
        list.add(mapRow(rs));
      }
      return list;
    }
  }

  public List<TopicInteraction> findByTopicAndType(int topicId, InteractionType type) throws SQLException {
    String sql = "SELECT * FROM topic_interaction WHERE topic_id = ? AND interaction = ? ORDER BY interacted_at DESC";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, topicId);
      stmt.setString(2, type.name());

      ResultSet rs = stmt.executeQuery();
      List<TopicInteraction> list = new ArrayList<>();
      while (rs.next()) {
        list.add(mapRow(rs));
      }
      return list;
    }
  }

  private TopicInteraction mapRow(ResultSet rs) throws SQLException {
    return new TopicInteraction(
        rs.getInt("interaction_id"),
        rs.getInt("user_id"),
        rs.getInt("topic_id"),
        InteractionType.valueOf(rs.getString("interaction")),
        rs.getTimestamp("interacted_at").toLocalDateTime()
    );
  }
}
